package com.anicictina.backend.studyplan.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyPlanGenerateRequest {

    private List<Long> subjectIds;
}
