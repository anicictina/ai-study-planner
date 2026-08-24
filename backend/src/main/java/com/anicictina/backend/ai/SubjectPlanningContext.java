package com.anicictina.backend.ai;

import com.anicictina.backend.subject.Level;
import java.time.LocalDate;
import java.util.List;

public record SubjectPlanningContext(
    Long subjectId,
    String subjectName,
    Level difficulty,
    Level priority,
    Integer knowledgePercent,
    LocalDate horizonEnd,
    List<String> completedTopics
) {
}
