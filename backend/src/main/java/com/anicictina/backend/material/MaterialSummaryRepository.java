package com.anicictina.backend.material;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialSummaryRepository extends JpaRepository<MaterialSummary, Long> {

    Optional<MaterialSummary> findByMaterialId(Long materialId);
}
