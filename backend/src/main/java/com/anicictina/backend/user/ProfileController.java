package com.anicictina.backend.user;

import com.anicictina.backend.user.dto.AvailabilitySlotRequest;
import com.anicictina.backend.user.dto.AvailabilitySlotResponse;
import com.anicictina.backend.user.dto.ChangePasswordRequest;
import com.anicictina.backend.user.dto.NameUpdateRequest;
import com.anicictina.backend.user.dto.ProfileUpdateRequest;
import com.anicictina.backend.user.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/availability")
    public List<AvailabilitySlotResponse> getAvailability() {
        return profileService.getAvailability();
    }

    @PutMapping("/availability")
    public List<AvailabilitySlotResponse> updateAvailability(@Valid @RequestBody List<AvailabilitySlotRequest> requests) {
        return profileService.updateAvailability(requests);
    }

    @PutMapping("/preferred-time")
    public UserResponse updatePreferredTime(@Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.updatePreferredTime(request);
    }

    @PutMapping("/name")
    public UserResponse updateName(@Valid @RequestBody NameUpdateRequest request) {
        return profileService.updateName(request);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
