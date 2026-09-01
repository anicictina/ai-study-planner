package com.anicictina.backend.studyplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.ai.GeminiClient;
import com.anicictina.backend.ai.PromptBuilder;
import com.anicictina.backend.ai.StudyPlanValidator;
import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.exam.ExamRepository;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.studyplan.dto.StudyPlanGenerateRequest;
import com.anicictina.backend.studysession.StudySession;
import com.anicictina.backend.studysession.StudySessionRepository;
import com.anicictina.backend.subject.Level;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.AvailabilitySlotRepository;
import com.anicictina.backend.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private StudyPlanValidator studyPlanValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StudyPlanService studyPlanService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        studyPlanService = new StudyPlanService(
            studyPlanRepository, subjectRepository, examRepository, availabilitySlotRepository,
            studySessionRepository, currentUserProvider, promptBuilder, geminiClient,
            studyPlanValidator, objectMapper);

        currentUser = User.builder().id(1L).email("owner@example.com").build();
    }

    private StudyPlan planOwnedBy(Long ownerId, StudyPlanStatus status) {
        StudyPlan plan = new StudyPlan();
        plan.setId(5L);
        plan.setUser(User.builder().id(ownerId).email("owner@example.com").build());
        plan.setStatus(status);
        return plan;
    }

    @Test
    void generateThrowsWhenNoActiveSubjects() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of());

        StudyPlanGenerateRequest request = new StudyPlanGenerateRequest();

        assertThrows(ValidationException.class, () -> studyPlanService.generate(request));
        verify(availabilitySlotRepository, never()).findByUserIdOrderByDayOfWeekAscStartTimeAsc(1L);
    }

    @Test
    void generateThrowsWhenNoAvailabilitySlotsConfigured() {
        Subject subject = Subject.builder()
            .id(2L).user(currentUser).name("Baze podataka")
            .difficulty(Level.MEDIUM).priority(Level.MEDIUM).knowledgePercent(40).color("#000").archived(false)
            .build();
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of(subject));
        when(availabilitySlotRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(1L)).thenReturn(List.of());

        StudyPlanGenerateRequest request = new StudyPlanGenerateRequest();

        assertThrows(ValidationException.class, () -> studyPlanService.generate(request));
        verify(geminiClient, never()).generateJson(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void generateFiltersToOnlyRequestedSubjectIds() {
        Subject wanted = Subject.builder()
            .id(2L).user(currentUser).name("Baze podataka")
            .difficulty(Level.MEDIUM).priority(Level.MEDIUM).knowledgePercent(40).color("#000").archived(false)
            .build();
        Subject notWanted = Subject.builder()
            .id(3L).user(currentUser).name("Algoritmi")
            .difficulty(Level.HIGH).priority(Level.HIGH).knowledgePercent(20).color("#111").archived(false)
            .build();
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of(wanted, notWanted));
        when(availabilitySlotRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(1L)).thenReturn(List.of());

        StudyPlanGenerateRequest request = new StudyPlanGenerateRequest();
        request.setSubjectIds(List.of(2L));

        // Only subject 2 is requested; since availability is still empty this throws before
        // reaching the AI call, but it must fail on the "no availability" message, not
        // "no active subjects" - proving the id filter kept subject 2 in the candidate list.
        ValidationException ex = assertThrows(ValidationException.class, () -> studyPlanService.generate(request));
        assertEquals(
            "Nisi podesila raspoloživo vreme za učenje. Idi na Profil i podesi ga pre generisanja plana.",
            ex.getMessage());
    }

    @Test
    void acceptThrowsWhenPlanAlreadyProcessed() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studyPlanRepository.findByIdAndUserId(5L, 1L))
            .thenReturn(Optional.of(planOwnedBy(1L, StudyPlanStatus.ACCEPTED)));

        assertThrows(ValidationException.class, () -> studyPlanService.accept(5L));
        verify(studySessionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptThrowsWhenPlanBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studyPlanRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studyPlanService.accept(5L));
    }

    @Test
    void acceptCreatesLinkedSessionForEachItemAndMarksPlanAccepted() {
        StudyPlan plan = planOwnedBy(1L, StudyPlanStatus.PENDING);
        Subject subject = Subject.builder().id(2L).user(currentUser).name("Baze podataka").color("#000").build();
        StudyPlanItem item = StudyPlanItem.builder()
            .subject(subject).itemDate(LocalDate.now()).startTime(LocalTime.of(17, 0)).durationMinutes(60).topic("SQL")
            .build();
        plan.addItem(item);

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studyPlanRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(plan));

        var response = studyPlanService.accept(5L);

        assertEquals(StudyPlanStatus.ACCEPTED, plan.getStatus());
        verify(studySessionRepository).save(org.mockito.ArgumentMatchers.any(StudySession.class));
        assertEquals(StudyPlanStatus.ACCEPTED, response.status());
    }

    @Test
    void discardThrowsWhenPlanAlreadyProcessed() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studyPlanRepository.findByIdAndUserId(5L, 1L))
            .thenReturn(Optional.of(planOwnedBy(1L, StudyPlanStatus.DISCARDED)));

        assertThrows(ValidationException.class, () -> studyPlanService.discard(5L));
    }

    @Test
    void discardSetsPlanStatusToDiscarded() {
        StudyPlan plan = planOwnedBy(1L, StudyPlanStatus.PENDING);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studyPlanRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(plan));

        studyPlanService.discard(5L);

        assertEquals(StudyPlanStatus.DISCARDED, plan.getStatus());
    }
}
