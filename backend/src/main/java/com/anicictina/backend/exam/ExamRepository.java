package com.anicictina.backend.exam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("""
        SELECT e FROM Exam e JOIN FETCH e.subject s
        WHERE s.user.id = :userId
        ORDER BY e.examDate ASC,
            CASE s.priority WHEN com.anicictina.backend.subject.Level.HIGH THEN 0
                             WHEN com.anicictina.backend.subject.Level.MEDIUM THEN 1
                             ELSE 2 END ASC
        """)
    List<Exam> findAllForUserOrderedByDateAndPriority(@Param("userId") Long userId);

    Optional<Exam> findByIdAndSubjectUserId(Long id, Long userId);
}
