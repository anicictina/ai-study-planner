package com.anicictina.backend.studysession;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.studysession.dto.StudySessionRequest;
import com.anicictina.backend.studysession.dto.StudySessionResponse;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<StudySessionResponse> findAll() {
        User user = currentUserProvider.getCurrentUser();
        return studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(user.getId()).stream()
            .map(StudySessionResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public StudySessionResponse findOne(Long id) {
        return StudySessionResponse.from(getOwnedSession(id));
    }

    @Transactional
    public StudySessionResponse create(StudySessionRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Subject subject = getOwnedSubject(request.getSubjectId(), user);

        StudySession session = StudySession.builder()
            .user(user)
            .subject(subject)
            .topic(request.getTopic())
            .sessionDate(request.getSessionDate())
            .startTime(request.getStartTime())
            .durationMinutes(request.getDurationMinutes())
            .completed(false)
            .activityType(request.getActivityType() != null ? request.getActivityType() : ActivityType.READING)
            .build();

        studySessionRepository.save(session);
        return StudySessionResponse.from(session);
    }

    @Transactional
    public StudySessionResponse update(Long id, StudySessionRequest request) {
        StudySession session = getOwnedSession(id);

        if (!session.getSubject().getId().equals(request.getSubjectId())) {
            session.setSubject(getOwnedSubject(request.getSubjectId(), session.getUser()));
        }

        session.setTopic(request.getTopic());
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setDurationMinutes(request.getDurationMinutes());
        if (request.getActivityType() != null) {
            session.setActivityType(request.getActivityType());
        }

        return StudySessionResponse.from(session);
    }

    @Transactional
    public void delete(Long id) {
        StudySession session = getOwnedSession(id);
        studySessionRepository.delete(session);
    }

    @Transactional
    public StudySessionResponse complete(Long id) {
        StudySession session = getOwnedSession(id);
        session.setCompleted(true);
        return StudySessionResponse.from(session);
    }

    private StudySession getOwnedSession(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return studySessionRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
    }

    private Subject getOwnedSubject(Long subjectId, User user) {
        return subjectRepository.findByIdAndUserId(subjectId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }
}
