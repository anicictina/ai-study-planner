package com.anicictina.backend.subject;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.dto.SubjectRequest;
import com.anicictina.backend.subject.dto.SubjectResponse;
import com.anicictina.backend.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private static final String DEFAULT_COLOR = "#3F51B5";

    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll(boolean archived) {
        User user = currentUserProvider.getCurrentUser();
        return subjectRepository.findByUserIdAndArchivedOrderByNameAsc(user.getId(), archived).stream()
            .map(SubjectResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse findOne(Long id) {
        return SubjectResponse.from(getOwnedSubject(id));
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Subject subject = Subject.builder()
            .user(user)
            .name(request.getName().trim())
            .description(request.getDescription())
            .credits(request.getCredits())
            .difficulty(request.getDifficulty())
            .priority(request.getPriority())
            .knowledgePercent(request.getKnowledgePercent() != null ? request.getKnowledgePercent() : 0)
            .color(request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : DEFAULT_COLOR)
            .archived(false)
            .build();

        subjectRepository.save(subject);
        return SubjectResponse.from(subject);
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getOwnedSubject(id);

        subject.setName(request.getName().trim());
        subject.setDescription(request.getDescription());
        subject.setCredits(request.getCredits());
        subject.setDifficulty(request.getDifficulty());
        subject.setPriority(request.getPriority());
        if (request.getKnowledgePercent() != null) {
            subject.setKnowledgePercent(request.getKnowledgePercent());
        }
        if (request.getColor() != null && !request.getColor().isBlank()) {
            subject.setColor(request.getColor());
        }

        return SubjectResponse.from(subject);
    }

    @Transactional
    public void delete(Long id) {
        Subject subject = getOwnedSubject(id);
        subjectRepository.delete(subject);
    }

    @Transactional
    public SubjectResponse archive(Long id) {
        Subject subject = getOwnedSubject(id);
        subject.setArchived(true);
        return SubjectResponse.from(subject);
    }

    private Subject getOwnedSubject(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return subjectRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }
}
