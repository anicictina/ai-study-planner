package com.anicictina.backend.statistics;

import com.anicictina.backend.exam.Exam;
import com.anicictina.backend.exam.ExamRepository;
import com.anicictina.backend.exam.ExamStatus;
import com.anicictina.backend.exam.dto.ExamResponse;
import com.anicictina.backend.material.MaterialRepository;
import com.anicictina.backend.material.MaterialStatus;
import com.anicictina.backend.material.StudyMaterial;
import com.anicictina.backend.quiz.QuizAttempt;
import com.anicictina.backend.quiz.QuizAttemptRepository;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.statistics.dto.AttemptScore;
import com.anicictina.backend.statistics.dto.DayMinutes;
import com.anicictina.backend.statistics.dto.MaterialProgressResponse;
import com.anicictina.backend.statistics.dto.QuizStatsResponse;
import com.anicictina.backend.statistics.dto.StatisticsOverviewResponse;
import com.anicictina.backend.statistics.dto.SubjectMinutes;
import com.anicictina.backend.statistics.dto.WeeklyStudyResponse;
import com.anicictina.backend.studysession.StudySession;
import com.anicictina.backend.studysession.StudySessionRepository;
import com.anicictina.backend.studysession.dto.StudySessionResponse;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final int UPCOMING_EXAMS_LIMIT = 5;
    private static final int RECENT_ATTEMPTS_LIMIT = 10;

    private final StudySessionRepository studySessionRepository;
    private final ExamRepository examRepository;
    private final MaterialRepository materialRepository;
    private final SubjectRepository subjectRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public StatisticsOverviewResponse getOverview() {
        User user = currentUserProvider.getCurrentUser();
        LocalDate today = LocalDate.now();

        List<StudySession> allSessions =
            studySessionRepository.findByUserIdOrderBySessionDateAscStartTimeAsc(user.getId());

        List<StudySessionResponse> todaySessions = allSessions.stream()
            .filter(session -> session.getSessionDate().equals(today))
            .map(StudySessionResponse::from)
            .toList();

        List<Exam> allExams = examRepository.findAllForUserOrderedByDateAndPriority(user.getId());
        List<ExamResponse> upcomingExams = allExams.stream()
            .filter(exam -> exam.getStatus() == ExamStatus.PLANNED)
            .filter(exam -> !exam.getExamDate().isBefore(today))
            .limit(UPCOMING_EXAMS_LIMIT)
            .map(ExamResponse::from)
            .toList();

        WeeklyStudyResponse weeklyStudy = buildWeeklyStudy(allSessions, today);

        List<Subject> subjects = subjectRepository.findByUserIdAndArchivedOrderByNameAsc(user.getId(), false);
        List<StudyMaterial> allMaterials = materialRepository.findBySubjectUserId(user.getId());
        List<MaterialProgressResponse> materialProgress = buildMaterialProgress(subjects, allMaterials);

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(user.getId());
        QuizStatsResponse quizStats = buildQuizStats(attempts);

        return StatisticsOverviewResponse.builder()
            .todaySessions(todaySessions)
            .upcomingExams(upcomingExams)
            .weeklyStudy(weeklyStudy)
            .materialProgress(materialProgress)
            .quizStats(quizStats)
            .build();
    }

    private WeeklyStudyResponse buildWeeklyStudy(List<StudySession> sessions, LocalDate today) {
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<StudySession> weekSessions = sessions.stream()
            .filter(StudySession::isCompleted)
            .filter(session -> !session.getSessionDate().isBefore(weekStart)
                && !session.getSessionDate().isAfter(weekEnd))
            .toList();

        Map<LocalDate, Integer> minutesByDay = new TreeMap<>();
        for (int i = 0; i < 7; i++) {
            minutesByDay.put(weekStart.plusDays(i), 0);
        }
        for (StudySession session : weekSessions) {
            minutesByDay.merge(session.getSessionDate(), session.getDurationMinutes(), Integer::sum);
        }

        Map<Long, SubjectMinutesAccumulator> minutesBySubject = new LinkedHashMap<>();
        for (StudySession session : weekSessions) {
            Subject subject = session.getSubject();
            minutesBySubject
                .computeIfAbsent(subject.getId(), id -> new SubjectMinutesAccumulator(subject.getName(), subject.getColor()))
                .minutes += session.getDurationMinutes();
        }

        List<DayMinutes> byDay = minutesByDay.entrySet().stream()
            .map(entry -> DayMinutes.builder().date(entry.getKey()).minutes(entry.getValue()).build())
            .toList();

        List<SubjectMinutes> bySubject = minutesBySubject.entrySet().stream()
            .map(entry -> SubjectMinutes.builder()
                .subjectId(entry.getKey())
                .subjectName(entry.getValue().name)
                .color(entry.getValue().color)
                .minutes(entry.getValue().minutes)
                .build())
            .toList();

        int totalMinutes = byDay.stream().mapToInt(DayMinutes::minutes).sum();

        return WeeklyStudyResponse.builder()
            .weekStart(weekStart)
            .totalMinutes(totalMinutes)
            .byDay(byDay)
            .bySubject(bySubject)
            .build();
    }

    private List<MaterialProgressResponse> buildMaterialProgress(List<Subject> subjects, List<StudyMaterial> materials) {
        return subjects.stream()
            .map(subject -> {
                List<StudyMaterial> subjectMaterials = materials.stream()
                    .filter(material -> material.getSubject().getId().equals(subject.getId()))
                    .toList();

                int total = subjectMaterials.size();
                int learned = (int) subjectMaterials.stream()
                    .filter(material -> material.getStatus() == MaterialStatus.LEARNED)
                    .count();
                int learnedPercent = total == 0 ? 0 : Math.round(learned * 100f / total);

                return MaterialProgressResponse.builder()
                    .subjectId(subject.getId())
                    .subjectName(subject.getName())
                    .color(subject.getColor())
                    .totalMaterials(total)
                    .learnedMaterials(learned)
                    .learnedPercent(learnedPercent)
                    .build();
            })
            .toList();
    }

    private QuizStatsResponse buildQuizStats(List<QuizAttempt> attemptsDesc) {
        if (attemptsDesc.isEmpty()) {
            return QuizStatsResponse.builder().averageScorePercent(0).recentAttempts(List.of()).build();
        }

        double average = attemptsDesc.stream()
            .mapToDouble(attempt -> attempt.getCorrectCount() * 100.0 / attempt.getTotalCount())
            .average()
            .orElse(0);

        List<AttemptScore> recentAttempts = attemptsDesc.stream()
            .limit(RECENT_ATTEMPTS_LIMIT)
            .map(attempt -> AttemptScore.builder()
                .attemptedAt(attempt.getAttemptedAt())
                .materialTitle(attempt.getQuiz().getMaterial().getTitle())
                .scorePercent(attempt.getCorrectCount() * 100.0 / attempt.getTotalCount())
                .build())
            .toList()
            .reversed();

        return QuizStatsResponse.builder()
            .averageScorePercent(Math.round(average * 10) / 10.0)
            .recentAttempts(recentAttempts)
            .build();
    }

    private static class SubjectMinutesAccumulator {
        private final String name;
        private final String color;
        private int minutes;

        SubjectMinutesAccumulator(String name, String color) {
            this.name = name;
            this.color = color;
        }
    }
}
