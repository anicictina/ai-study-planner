package com.anicictina.backend.reminder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.reminder.dto.ReminderRequest;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ReminderService reminderService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
    }

    private Reminder reminderOwnedBy(Long ownerId) {
        User owner = User.builder().id(ownerId).email("owner@example.com").build();
        return Reminder.builder()
            .id(5L)
            .user(owner)
            .message("Uci algoritme")
            .remindAt(LocalDateTime.now().plusHours(2))
            .dismissed(false)
            .build();
    }

    @Test
    void dismissThrowsWhenReminderBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        // findByIdAndUserId is scoped by the current user's id, so another user's
        // reminder simply never matches - this is what prevents cross-user access.
        when(reminderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.dismiss(5L));
        verify(reminderRepository).findByIdAndUserId(5L, 1L);
    }

    @Test
    void dismissSucceedsWhenReminderOwnedByCurrentUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(reminderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(reminderOwnedBy(1L)));

        var response = reminderService.dismiss(5L);

        assertEquals(true, response.dismissed());
    }

    @Test
    void deleteThrowsWhenReminderBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(reminderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.delete(5L));
    }

    @Test
    void updateThrowsWhenReminderBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(reminderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        ReminderRequest request = new ReminderRequest();
        request.setMessage("Pokusaj izmene tudjeg podsetnika");
        request.setRemindAt(LocalDateTime.now().plusHours(1));

        assertThrows(ResourceNotFoundException.class, () -> reminderService.update(5L, request));
    }

    @Test
    void createAssignsCurrentUserAsOwner() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        ReminderRequest request = new ReminderRequest();
        request.setMessage("Uci algoritme");
        request.setRemindAt(LocalDateTime.now().plusHours(3));

        reminderService.create(request);

        var captor = org.mockito.ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUser().getId());
    }
}
