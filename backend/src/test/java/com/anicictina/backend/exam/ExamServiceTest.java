package com.anicictina.backend.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.exam.dto.ExamRequest;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Level;
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
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ExamService examService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
    }

    private Exam examFor(Subject subject) {
        return Exam.builder()
            .id(7L)
            .subject(subject)
            .examDate(LocalDate.now().plusDays(10))
            .status(ExamStatus.PLANNED)
            .build();
    }

    @Test
    void findOneThrowsWhenExamBelongsToSubjectOfAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        // findByIdAndSubjectUserId is scoped by the current user's id at the query level,
        // so an exam whose subject belongs to a different user never comes back.
        when(examRepository.findByIdAndSubjectUserId(7L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.findOne(7L));
        verify(examRepository).findByIdAndSubjectUserId(7L, 1L);
    }

    @Test
    void findOneReturnsExamWhenSubjectOwnedByCurrentUser() {
        Subject subject = Subject.builder().id(3L).user(currentUser).name("Baze podataka").color("#000").build();
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(examRepository.findByIdAndSubjectUserId(7L, 1L)).thenReturn(Optional.of(examFor(subject)));

        var response = examService.findOne(7L);

        assertEquals(7L, response.id());
    }

    @Test
    void deleteThrowsWhenExamBelongsToSubjectOfAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(examRepository.findByIdAndSubjectUserId(7L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.delete(7L));
    }

    @Test
    void createThrowsWhenTargetSubjectBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        // create() resolves the subject via findByIdAndUserId, so pointing at someone
        // else's subject id must fail instead of silently attaching the exam to it.
        when(subjectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ExamRequest request = new ExamRequest();
        request.setSubjectId(99L);
        request.setExamDate(LocalDate.now().plusDays(5));
        request.setStatus(ExamStatus.PLANNED);

        assertThrows(ResourceNotFoundException.class, () -> examService.create(request));
    }
}
