package com.anicictina.backend.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.anicictina.backend.exam.ExamRepository;
import com.anicictina.backend.material.MaterialRepository;
import com.anicictina.backend.material.MaterialStatus;
import com.anicictina.backend.material.StudyMaterial;
import com.anicictina.backend.quiz.Quiz;
import com.anicictina.backend.quiz.QuizAttempt;
import com.anicictina.backend.quiz.QuizAttemptRepository;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.studysession.ActivityType;
import com.anicictina.backend.studysession.StudySession;
import com.anicictina.backend.studysession.StudySessionRepository;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private StatisticsService statisticsService;

    private User currentUser;
    private Subject subject;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
        subject = Subject.builder().id(2L).user(currentUser).name("Baze podataka").color("#000").build();

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(examRepository.findAllForUserOrderedByDateAndPriority(1L)).thenReturn(List.of());
    }

    private StudySession session(LocalDate date, int minutes, boolean completed) {
        return StudySession.builder()
            .id(1L).user(currentUser).subject(subject).sessionDate(date)
            .durationMinutes(minutes).completed(completed).activityType(ActivityType.READING)
            .build();
    }

    @Test
    void weeklyStudyOnlyCountsCompletedSessionsWithinCurrentWeek() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        when(studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(1L)).thenReturn(List.of(
            session(monday, 60, true),
            session(monday.plusDays(1), 30, false),
            session(monday.minusDays(7), 45, true)
        ));
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of());
        when(materialRepository.findBySubjectUserId(1L)).thenReturn(List.of());
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of());

        var overview = statisticsService.getOverview();

        assertEquals(60, overview.weeklyStudy().totalMinutes());
    }

    @Test
    void materialProgressComputesRoundedPercentPerSubject() {
        StudyMaterial learned = StudyMaterial.builder().id(1L).subject(subject).title("A").content("x").status(MaterialStatus.LEARNED).build();
        StudyMaterial notStarted = StudyMaterial.builder().id(2L).subject(subject).title("B").content("y").status(MaterialStatus.NOT_STARTED).build();
        StudyMaterial inProgress = StudyMaterial.builder().id(3L).subject(subject).title("C").content("z").status(MaterialStatus.IN_PROGRESS).build();

        when(studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(1L)).thenReturn(List.of());
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of(subject));
        when(materialRepository.findBySubjectUserId(1L)).thenReturn(List.of(learned, notStarted, inProgress));
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of());

        var overview = statisticsService.getOverview();

        assertEquals(1, overview.materialProgress().size());
        assertEquals(33, overview.materialProgress().get(0).learnedPercent());
    }

    @Test
    void quizStatsReturnsZeroAverageWhenNoAttempts() {
        when(studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(1L)).thenReturn(List.of());
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of());
        when(materialRepository.findBySubjectUserId(1L)).thenReturn(List.of());
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of());

        var overview = statisticsService.getOverview();

        assertEquals(0, overview.quizStats().averageScorePercent());
    }

    @Test
    void quizStatsComputesAverageAcrossAttempts() {
        StudyMaterial material = StudyMaterial.builder().id(1L).subject(subject).title("Predavanje").content("x").build();
        Quiz quiz = new Quiz();
        quiz.setMaterial(material);
        quiz.setUser(currentUser);

        QuizAttempt attempt1 = QuizAttempt.builder().quiz(quiz).user(currentUser).correctCount(8).totalCount(10).attemptedAt(Instant.now()).build();
        QuizAttempt attempt2 = QuizAttempt.builder().quiz(quiz).user(currentUser).correctCount(4).totalCount(10).attemptedAt(Instant.now()).build();

        when(studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(1L)).thenReturn(List.of());
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(1L, false)).thenReturn(List.of());
        when(materialRepository.findBySubjectUserId(1L)).thenReturn(List.of());
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of(attempt1, attempt2));

        var overview = statisticsService.getOverview();

        assertEquals(60.0, overview.quizStats().averageScorePercent());
        assertEquals(2, overview.quizStats().recentAttempts().size());
    }
}
