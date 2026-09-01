package com.anicictina.backend.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.dto.SubjectRequest;
import com.anicictina.backend.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SubjectService subjectService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
    }

    private Subject subjectOwnedBy(Long ownerId) {
        User owner = User.builder().id(ownerId).email("owner@example.com").build();
        return Subject.builder()
            .id(42L)
            .user(owner)
            .name("Baze podataka")
            .credits(6)
            .difficulty(Level.MEDIUM)
            .priority(Level.MEDIUM)
            .knowledgePercent(0)
            .color("#3F51B5")
            .archived(false)
            .build();
    }

    @Test
    void findOneReturnsSubjectWhenOwnedByCurrentUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(42L, 1L)).thenReturn(Optional.of(subjectOwnedBy(1L)));

        var response = subjectService.findOne(42L);

        assertEquals(42L, response.id());
    }

    @Test
    void findOneThrowsWhenSubjectBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        // The repository query is scoped by the current user's id, so another user's
        // subject simply never matches - this is what actually prevents cross-user access.
        when(subjectRepository.findByIdAndUserId(42L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.findOne(42L));
        verify(subjectRepository).findByIdAndUserId(eq(42L), eq(1L));
    }

    @Test
    void deleteThrowsWhenSubjectBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(42L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.delete(42L));
    }

    @Test
    void updateThrowsWhenSubjectBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(42L, 1L)).thenReturn(Optional.empty());

        SubjectRequest request = new SubjectRequest();
        request.setName("Pokušaj izmene tuđeg predmeta");
        request.setCredits(6);
        request.setDifficulty(Level.LOW);
        request.setPriority(Level.LOW);

        assertThrows(ResourceNotFoundException.class, () -> subjectService.update(42L, request));
    }

    @Test
    void createAssignsCurrentUserAsOwner() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        SubjectRequest request = new SubjectRequest();
        request.setName("Algoritmi");
        request.setCredits(6);
        request.setDifficulty(Level.HIGH);
        request.setPriority(Level.HIGH);

        subjectService.create(request);

        var captor = org.mockito.ArgumentCaptor.forClass(Subject.class);
        verify(subjectRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUser().getId());
    }

    @Test
    void findAllQueriesOnlyCurrentUsersSubjects() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByUserIdAndArchivedOrderByNameAsc(any(), anyBoolean())).thenReturn(java.util.List.of());

        subjectService.findAll(false);

        verify(subjectRepository).findByUserIdAndArchivedOrderByNameAsc(1L, false);
    }
}
