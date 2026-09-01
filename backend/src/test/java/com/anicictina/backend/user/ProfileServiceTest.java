package com.anicictina.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.user.dto.AvailabilitySlotRequest;
import com.anicictina.backend.user.dto.ChangePasswordRequest;
import com.anicictina.backend.user.dto.NameUpdateRequest;
import jakarta.validation.ValidationException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
            .id(1L)
            .email("tina@example.com")
            .firstName("Tina")
            .lastName("Anicic")
            .passwordHash("hashed-old-password")
            .role(Role.STUDENT)
            .build();
    }

    @Test
    void changePasswordThrowsWhenCurrentPasswordIsWrong() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("wrong", "hashed-old-password")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newSecurePassword1");

        assertThrows(ValidationException.class, () -> profileService.changePassword(request));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePasswordEncodesAndStoresNewPasswordWhenCurrentMatches() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("correct", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword1")).thenReturn("hashed-new-password");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("correct");
        request.setNewPassword("newSecurePassword1");

        profileService.changePassword(request);

        assertEquals("hashed-new-password", currentUser.getPasswordHash());
    }

    @Test
    void updateAvailabilityThrowsWhenEndTimeNotAfterStartTime() {
        AvailabilitySlotRequest request = new AvailabilitySlotRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(17, 0));

        assertThrows(ValidationException.class, () -> profileService.updateAvailability(List.of(request)));
        verify(availabilitySlotRepository, never()).deleteByUserId(any());
    }

    @Test
    void updateNameTrimsAndSetsFirstAndLastName() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        NameUpdateRequest request = new NameUpdateRequest();
        request.setFirstName("  Tina  ");
        request.setLastName("  Anicic Nova  ");

        var response = profileService.updateName(request);

        assertEquals("Tina", response.firstName());
        assertEquals("Anicic Nova", response.lastName());
    }
}
