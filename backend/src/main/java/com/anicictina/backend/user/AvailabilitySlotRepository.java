package com.anicictina.backend.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    @Query("""
        SELECT a FROM AvailabilitySlot a
        WHERE a.user.id = :userId
        ORDER BY
            CASE a.dayOfWeek
                WHEN java.time.DayOfWeek.MONDAY THEN 1
                WHEN java.time.DayOfWeek.TUESDAY THEN 2
                WHEN java.time.DayOfWeek.WEDNESDAY THEN 3
                WHEN java.time.DayOfWeek.THURSDAY THEN 4
                WHEN java.time.DayOfWeek.FRIDAY THEN 5
                WHEN java.time.DayOfWeek.SATURDAY THEN 6
                ELSE 7
            END ASC,
            a.startTime ASC
        """)
    List<AvailabilitySlot> findByUserIdOrderByDayOfWeekAscStartTimeAsc(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM AvailabilitySlot a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
