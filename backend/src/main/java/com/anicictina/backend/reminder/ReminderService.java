package com.anicictina.backend.reminder;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.reminder.dto.ReminderRequest;
import com.anicictina.backend.reminder.dto.ReminderResponse;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<ReminderResponse> findAll() {
        User user = currentUserProvider.getCurrentUser();
        return reminderRepository.findByUserIdOrderByRemindAtAsc(user.getId()).stream()
            .map(ReminderResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> findDue() {
        User user = currentUserProvider.getCurrentUser();
        return reminderRepository
            .findByUserIdAndDismissedFalseAndRemindAtLessThanEqualOrderByRemindAtAsc(
                user.getId(), LocalDateTime.now())
            .stream()
            .map(ReminderResponse::from)
            .toList();
    }

    @Transactional
    public ReminderResponse create(ReminderRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Subject subject = request.getSubjectId() != null ? getOwnedSubject(request.getSubjectId(), user) : null;

        Reminder reminder = Reminder.builder()
            .user(user)
            .subject(subject)
            .message(request.getMessage().trim())
            .remindAt(request.getRemindAt())
            .dismissed(false)
            .build();

        reminderRepository.save(reminder);
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public ReminderResponse update(Long id, ReminderRequest request) {
        Reminder reminder = getOwnedReminder(id);
        User user = currentUserProvider.getCurrentUser();

        Subject subject = request.getSubjectId() != null ? getOwnedSubject(request.getSubjectId(), user) : null;

        reminder.setSubject(subject);
        reminder.setMessage(request.getMessage().trim());
        reminder.setRemindAt(request.getRemindAt());

        return ReminderResponse.from(reminder);
    }

    @Transactional
    public ReminderResponse dismiss(Long id) {
        Reminder reminder = getOwnedReminder(id);
        reminder.setDismissed(true);
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public void delete(Long id) {
        Reminder reminder = getOwnedReminder(id);
        reminderRepository.delete(reminder);
    }

    private Reminder getOwnedReminder(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return reminderRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
    }

    private Subject getOwnedSubject(Long subjectId, User user) {
        return subjectRepository.findByIdAndUserId(subjectId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }
}
