package com.anicictina.backend.material;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<StudyMaterial, Long> {

    List<StudyMaterial> findBySubjectIdOrderByCreatedAtDesc(Long subjectId);

    List<StudyMaterial> findBySubjectUserId(Long userId);

    Optional<StudyMaterial> findByIdAndSubjectUserId(Long id, Long userId);
}
