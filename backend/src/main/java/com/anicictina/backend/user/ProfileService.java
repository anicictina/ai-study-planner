package com.anicictina.backend.user;

import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.user.dto.AvailabilitySlotRequest;
import com.anicictina.backend.user.dto.AvailabilitySlotResponse;
import com.anicictina.backend.user.dto.ProfileUpdateRequest;
import com.anicictina.backend.user.dto.UserResponse;
import jakarta.validation.ValidationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserProvider currentUserProvider;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailability() {
        User user = currentUserProvider.getCurrentUser();
        return availabilitySlotRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId()).stream()
            .map(AvailabilitySlotResponse::from)
            .toList();
    }

    @Transactional
    public List<AvailabilitySlotResponse> updateAvailability(List<AvailabilitySlotRequest> requests) {
        for (AvailabilitySlotRequest request : requests) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new ValidationException(
                    "End time must be after start time for " + request.getDayOfWeek());
            }
        }

        User user = currentUserProvider.getCurrentUser();
        availabilitySlotRepository.deleteByUserId(user.getId());

        List<AvailabilitySlot> slots = requests.stream()
            .map(request -> AvailabilitySlot.builder()
                .user(user)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build())
            .toList();

        availabilitySlotRepository.saveAll(slots);

        return slots.stream().map(AvailabilitySlotResponse::from).toList();
    }

    @Transactional
    public UserResponse updatePreferredTime(ProfileUpdateRequest request) {
        User user = currentUserProvider.getCurrentUser();
        user.setPreferredStudyTime(request.getPreferredStudyTime());
        return UserResponse.from(user);
    }
}
