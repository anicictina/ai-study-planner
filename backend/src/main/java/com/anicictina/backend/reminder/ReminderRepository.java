package com.anicictina.backend.reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);

    List<Reminder> findByUserIdAndDismissedFalseAndRemindAtLessThanEqualOrderByRemindAtAsc(
        Long userId, LocalDateTime now);

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);
}
