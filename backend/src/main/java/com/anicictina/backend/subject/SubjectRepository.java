package com.anicictina.backend.subject;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByUserIdAndArchivedOrderByNameAsc(Long userId, boolean archived);

    Optional<Subject> findByIdAndUserId(Long id, Long userId);
}
