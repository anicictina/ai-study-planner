package com.anicictina.backend.material;

import com.anicictina.backend.material.dto.MaterialRequest;
import com.anicictina.backend.material.dto.MaterialResponse;
import com.anicictina.backend.material.dto.MaterialStatusUpdateRequest;
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
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/subject/{subjectId}")
    public List<MaterialResponse> findAllForSubject(@PathVariable Long subjectId) {
        return materialService.findAllForSubject(subjectId);
    }

    @GetMapping("/{id}")
    public MaterialResponse findOne(@PathVariable Long id) {
        return materialService.findOne(id);
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> create(@Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.create(request));
    }

    @PutMapping("/{id}")
    public MaterialResponse update(@PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        return materialService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public MaterialResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody MaterialStatusUpdateRequest request
    ) {
        return materialService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
