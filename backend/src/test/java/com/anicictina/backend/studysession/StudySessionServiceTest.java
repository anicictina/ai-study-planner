package com.anicictina.backend.studysession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.studysession.dto.StudySessionRequest;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private StudySessionService studySessionService;

    private User currentUser;
    private Subject ownedSubject;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
        ownedSubject = Subject.builder().id(3L).user(currentUser).name("Baze podataka").color("#000").build();
    }

    private StudySession sessionOwnedBy(Long ownerId) {
        User owner = User.builder().id(ownerId).email("owner@example.com").build();
        return StudySession.builder()
            .id(9L)
            .user(owner)
            .subject(ownedSubject)
            .sessionDate(LocalDate.now())
            .durationMinutes(60)
            .completed(false)
            .activityType(ActivityType.READING)
            .build();
    }

    @Test
    void findOneThrowsWhenSessionBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studySessionRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studySessionService.findOne(9L));
        verify(studySessionRepository).findByIdAndUserId(9L, 1L);
    }

    @Test
    void completeMarksOwnedSessionAsCompleted() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studySessionRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(sessionOwnedBy(1L)));

        var response = studySessionService.complete(9L);

        assertTrue(response.completed());
    }

    @Test
    void completeThrowsWhenSessionBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(studySessionRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studySessionService.complete(9L));
    }

    @Test
    void createThrowsWhenTargetSubjectBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(99L);
        request.setSessionDate(LocalDate.now());
        request.setDurationMinutes(30);

        assertThrows(ResourceNotFoundException.class, () -> studySessionService.create(request));
    }

    @Test
    void createDefaultsActivityTypeToReadingWhenNotProvided() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(ownedSubject));

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(3L);
        request.setSessionDate(LocalDate.now());
        request.setDurationMinutes(45);

        var response = studySessionService.create(request);

        assertEquals(ActivityType.READING, response.activityType());
    }
}
