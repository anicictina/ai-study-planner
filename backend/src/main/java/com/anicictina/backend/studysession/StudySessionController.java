package com.anicictina.backend.studysession;

import com.anicictina.backend.studysession.dto.StudySessionRequest;
import com.anicictina.backend.studysession.dto.StudySessionResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @GetMapping
    public List<StudySessionResponse> findAll() {
        return studySessionService.findAll();
    }

    @GetMapping("/{id}")
    public StudySessionResponse findOne(@PathVariable Long id) {
        return studySessionService.findOne(id);
    }

    @PostMapping
    public ResponseEntity<StudySessionResponse> create(@Valid @RequestBody StudySessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studySessionService.create(request));
    }

    @PutMapping("/{id}")
    public StudySessionResponse update(@PathVariable Long id, @Valid @RequestBody StudySessionRequest request) {
        return studySessionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studySessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public StudySessionResponse complete(@PathVariable Long id) {
        return studySessionService.complete(id);
    }
}
