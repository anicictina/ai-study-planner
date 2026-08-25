package com.anicictina.backend.reminder;

import com.anicictina.backend.reminder.dto.ReminderRequest;
import com.anicictina.backend.reminder.dto.ReminderResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public List<ReminderResponse> findAll() {
        return reminderService.findAll();
    }

    @GetMapping("/due")
    public List<ReminderResponse> findDue() {
        return reminderService.findDue();
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> create(@Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.create(request));
    }

    @PutMapping("/{id}")
    public ReminderResponse update(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) {
        return reminderService.update(id, request);
    }

    @PatchMapping("/{id}/dismiss")
    public ReminderResponse dismiss(@PathVariable Long id) {
        return reminderService.dismiss(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reminderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
