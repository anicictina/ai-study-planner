package com.anicictina.backend.material;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.material.dto.MaterialRequest;
import com.anicictina.backend.material.dto.MaterialResponse;
import com.anicictina.backend.material.dto.MaterialStatusUpdateRequest;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<MaterialResponse> findAllForSubject(Long subjectId) {
        User user = currentUserProvider.getCurrentUser();
        getOwnedSubject(subjectId, user);

        return materialRepository.findBySubjectIdOrderByCreatedAtDesc(subjectId).stream()
            .map(MaterialResponse::summaryFrom)
            .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse findOne(Long id) {
        return MaterialResponse.from(getOwnedMaterial(id));
    }

    @Transactional
    public MaterialResponse create(MaterialRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Subject subject = getOwnedSubject(request.getSubjectId(), user);

        StudyMaterial material = StudyMaterial.builder()
            .subject(subject)
            .title(request.getTitle().trim())
            .content(request.getContent())
            .status(MaterialStatus.NOT_STARTED)
            .build();

        materialRepository.save(material);
        return MaterialResponse.from(material);
    }

    @Transactional
    public MaterialResponse update(Long id, MaterialRequest request) {
        StudyMaterial material = getOwnedMaterial(id);

        material.setTitle(request.getTitle().trim());
        material.setContent(request.getContent());

        return MaterialResponse.from(material);
    }

    @Transactional
    public MaterialResponse updateStatus(Long id, MaterialStatusUpdateRequest request) {
        StudyMaterial material = getOwnedMaterial(id);
        material.setStatus(request.getStatus());
        return MaterialResponse.from(material);
    }

    @Transactional
    public void delete(Long id) {
        StudyMaterial material = getOwnedMaterial(id);
        materialRepository.delete(material);
    }

    private StudyMaterial getOwnedMaterial(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return materialRepository.findByIdAndSubjectUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
    }

    private Subject getOwnedSubject(Long subjectId, User user) {
        return subjectRepository.findByIdAndUserId(subjectId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }
}
