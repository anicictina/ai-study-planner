package com.anicictina.backend.quiz;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByIdAndUserId(Long id, Long userId);
}
