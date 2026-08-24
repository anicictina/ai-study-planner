package com.anicictina.backend.statistics.dto;

import com.anicictina.backend.exam.dto.ExamResponse;
import com.anicictina.backend.studysession.dto.StudySessionResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record StatisticsOverviewResponse(
    List<StudySessionResponse> todaySessions,
    List<ExamResponse> upcomingExams,
    WeeklyStudyResponse weeklyStudy,
    List<MaterialProgressResponse> materialProgress,
    QuizStatsResponse quizStats
) {
}
