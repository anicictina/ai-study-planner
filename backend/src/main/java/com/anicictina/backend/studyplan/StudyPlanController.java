package com.anicictina.backend.studyplan;

import com.anicictina.backend.studyplan.dto.StudyPlanGenerateRequest;
import com.anicictina.backend.studyplan.dto.StudyPlanResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping("/generate")
    public StudyPlanResponse generate(@RequestBody(required = false) StudyPlanGenerateRequest request) {
        return studyPlanService.generate(request != null ? request : new StudyPlanGenerateRequest());
    }

    @PostMapping("/recalculate")
    public StudyPlanResponse recalculate(@RequestBody(required = false) StudyPlanGenerateRequest request) {
        return studyPlanService.generate(request != null ? request : new StudyPlanGenerateRequest());
    }

    @GetMapping("/current")
    public ResponseEntity<StudyPlanResponse> getCurrent() {
        return studyPlanService.getCurrent()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public List<StudyPlanResponse> getHistory() {
        return studyPlanService.getHistory();
    }

    @PostMapping("/{id}/accept")
    public StudyPlanResponse accept(@PathVariable Long id) {
        return studyPlanService.accept(id);
    }

    @PostMapping("/{id}/discard")
    public StudyPlanResponse discard(@PathVariable Long id) {
        return studyPlanService.discard(id);
    }
}
