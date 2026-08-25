package com.anicictina.backend.studyplan;

import com.anicictina.backend.ai.AIServiceException;
import com.anicictina.backend.ai.AvailabilityWindow;
import com.anicictina.backend.ai.GeminiClient;
import com.anicictina.backend.ai.PromptBuilder;
import com.anicictina.backend.ai.RawProposedItem;
import com.anicictina.backend.ai.StudyPlanValidationOutcome;
import com.anicictina.backend.ai.StudyPlanValidator;
import com.anicictina.backend.ai.SubjectPlanningContext;
import com.anicictina.backend.ai.ValidatedStudyPlanItem;
import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.exam.Exam;
import com.anicictina.backend.exam.ExamRepository;
import com.anicictina.backend.exam.ExamStatus;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.studyplan.dto.StudyPlanGenerateRequest;
import com.anicictina.backend.studyplan.dto.StudyPlanResponse;
import com.anicictina.backend.studysession.ActivityType;
import com.anicictina.backend.studysession.StudySession;
import com.anicictina.backend.studysession.StudySessionRepository;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.AvailabilitySlot;
import com.anicictina.backend.user.AvailabilitySlotRepository;
import com.anicictina.backend.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private static final int DEFAULT_HORIZON_DAYS = 14;

    private final StudyPlanRepository studyPlanRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final StudySessionRepository studySessionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PromptBuilder promptBuilder;
    private final GeminiClient geminiClient;
    private final StudyPlanValidator studyPlanValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public StudyPlanResponse generate(StudyPlanGenerateRequest request) {
        User user = currentUserProvider.getCurrentUser();
        List<Subject> subjects = resolveSubjects(user, request.getSubjectIds());

        if (subjects.isEmpty()) {
            throw new ValidationException("Nema aktivnih predmeta za planiranje. Dodaj bar jedan predmet.");
        }

        List<AvailabilitySlot> availabilitySlots =
            availabilitySlotRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId());

        if (availabilitySlots.isEmpty()) {
            throw new ValidationException(
                "Nisi podesila raspoloživo vreme za učenje. Idi na Profil i podesi ga pre generisanja plana.");
        }

        LocalDate today = LocalDate.now();
        List<Exam> userExams = examRepository.findAllForUserOrderedByDateAndPriority(user.getId());
        List<StudySession> userSessions =
            studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(user.getId());

        List<SubjectPlanningContext> subjectContexts = subjects.stream()
            .map(subject -> buildSubjectContext(subject, today, userExams, userSessions))
            .toList();

        List<AvailabilityWindow> availabilityWindows = availabilitySlots.stream()
            .map(slot -> new AvailabilityWindow(slot.getDayOfWeek(), slot.getStartTime(), slot.getEndTime()))
            .toList();

        String prompt =
            promptBuilder.build(subjectContexts, availabilityWindows, user.getPreferredStudyTime(), today);
        String rawJson = geminiClient.generateJson(prompt, promptBuilder.buildResponseSchema());
        List<RawProposedItem> proposedItems = parseProposedItems(rawJson);

        StudyPlanValidationOutcome outcome =
            studyPlanValidator.validate(proposedItems, subjectContexts, availabilityWindows, today);

        Map<Long, Subject> subjectsById = subjects.stream()
            .collect(Collectors.toMap(Subject::getId, s -> s));

        StudyPlan plan = new StudyPlan();
        plan.setUser(user);
        plan.setRejectedItemsCount(outcome.rejectedItems().size());
        plan.setValidationNotes(buildValidationNotes(outcome));

        for (ValidatedStudyPlanItem validated : outcome.validItems()) {
            StudyPlanItem item = StudyPlanItem.builder()
                .subject(subjectsById.get(validated.subjectId()))
                .itemDate(validated.date())
                .startTime(validated.startTime())
                .durationMinutes(validated.durationMinutes())
                .topic(validated.topic())
                .build();
            plan.addItem(item);
        }

        studyPlanRepository.save(plan);
        return StudyPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public Optional<StudyPlanResponse> getCurrent() {
        User user = currentUserProvider.getCurrentUser();
        return studyPlanRepository.findFirstByUserIdOrderByGeneratedAtDesc(user.getId())
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> getHistory() {
        User user = currentUserProvider.getCurrentUser();
        return studyPlanRepository.findByUserIdOrderByGeneratedAtDesc(user.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public StudyPlanResponse accept(Long planId) {
        StudyPlan plan = getOwnedPlan(planId);

        if (plan.getStatus() != StudyPlanStatus.PENDING) {
            throw new ValidationException("Plan je već obrađen.");
        }

        for (StudyPlanItem item : plan.getItems()) {
            StudySession session = StudySession.builder()
                .user(plan.getUser())
                .subject(item.getSubject())
                .topic(item.getTopic())
                .sessionDate(item.getItemDate())
                .startTime(item.getStartTime())
                .durationMinutes(item.getDurationMinutes())
                .completed(false)
                .activityType(ActivityType.READING)
                .build();
            studySessionRepository.save(session);
            item.setLinkedSessionId(session.getId());
        }

        plan.setStatus(StudyPlanStatus.ACCEPTED);
        return toResponse(plan);
    }

    @Transactional
    public StudyPlanResponse discard(Long planId) {
        StudyPlan plan = getOwnedPlan(planId);

        if (plan.getStatus() != StudyPlanStatus.PENDING) {
            throw new ValidationException("Plan je već obrađen.");
        }

        plan.setStatus(StudyPlanStatus.DISCARDED);
        return StudyPlanResponse.from(plan);
    }

    private StudyPlanResponse toResponse(StudyPlan plan) {
        if (plan.getStatus() != StudyPlanStatus.ACCEPTED) {
            return StudyPlanResponse.from(plan);
        }

        List<Long> sessionIds = plan.getItems().stream()
            .map(StudyPlanItem::getLinkedSessionId)
            .filter(Objects::nonNull)
            .toList();

        Map<Long, Boolean> completedBySessionId = studySessionRepository.findAllById(sessionIds).stream()
            .collect(Collectors.toMap(StudySession::getId, StudySession::isCompleted));

        return StudyPlanResponse.fromAccepted(plan, completedBySessionId, LocalDate.now());
    }

    private StudyPlan getOwnedPlan(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return studyPlanRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
    }

    private List<Subject> resolveSubjects(User user, List<Long> subjectIds) {
        List<Subject> activeSubjects = subjectRepository.findByUserIdAndArchivedOrderByNameAsc(user.getId(), false);

        if (subjectIds == null || subjectIds.isEmpty()) {
            return activeSubjects;
        }

        Set<Long> requestedIds = new HashSet<>(subjectIds);
        return activeSubjects.stream().filter(s -> requestedIds.contains(s.getId())).toList();
    }

    private SubjectPlanningContext buildSubjectContext(
        Subject subject,
        LocalDate today,
        List<Exam> userExams,
        List<StudySession> userSessions
    ) {
        LocalDate horizonEnd = userExams.stream()
            .filter(exam -> exam.getSubject().getId().equals(subject.getId()))
            .filter(exam -> exam.getStatus() == ExamStatus.PLANNED)
            .filter(exam -> !exam.getExamDate().isBefore(today))
            .map(Exam::getExamDate)
            .min(LocalDate::compareTo)
            .orElse(today.plusDays(DEFAULT_HORIZON_DAYS));

        List<String> completedTopics = userSessions.stream()
            .filter(session -> session.getSubject().getId().equals(subject.getId()))
            .filter(StudySession::isCompleted)
            .map(StudySession::getTopic)
            .filter(topic -> topic != null && !topic.isBlank())
            .toList();

        return new SubjectPlanningContext(
            subject.getId(),
            subject.getName(),
            subject.getDifficulty(),
            subject.getPriority(),
            subject.getKnowledgePercent(),
            horizonEnd,
            completedTopics
        );
    }

    private List<RawProposedItem> parseProposedItems(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<RawProposedItem>>() { });
        } catch (IOException e) {
            throw new AIServiceException("AI servis je vratio neočekivan format odgovora.", e);
        }
    }

    private String buildValidationNotes(StudyPlanValidationOutcome outcome) {
        if (outcome.rejectedItems().isEmpty()) {
            return null;
        }

        return outcome.rejectedItems().stream()
            .map(rejected -> rejected.item().topic() + ": " + rejected.reason())
            .collect(Collectors.joining("; "));
    }
}
