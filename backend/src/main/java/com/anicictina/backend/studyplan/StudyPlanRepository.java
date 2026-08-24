package com.anicictina.backend.studyplan;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByUserIdOrderByGeneratedAtDesc(Long userId);

    Optional<StudyPlan> findFirstByUserIdOrderByGeneratedAtDesc(Long userId);

    Optional<StudyPlan> findByIdAndUserId(Long id, Long userId);
}
