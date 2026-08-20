package com.anicictina.backend.studysession;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUserIdOrderBySessionDateAscStartTimeAsc(Long userId);

    Optional<StudySession> findByIdAndUserId(Long id, Long userId);
}
