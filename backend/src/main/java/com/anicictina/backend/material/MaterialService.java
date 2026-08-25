package com.anicictina.backend.material;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.material.dto.MaterialRequest;
import com.anicictina.backend.material.dto.MaterialResponse;
import com.anicictina.backend.material.dto.MaterialStatusUpdateRequest;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public MaterialResponse uploadFile(Long subjectId, String title, MultipartFile file) {
        User user = currentUserProvider.getCurrentUser();
        Subject subject = getOwnedSubject(subjectId, user);

        String extractedText = extractText(file);
        if (extractedText.isBlank()) {
            throw new ValidationException("Nije moguće izvući tekst iz ovog fajla.");
        }

        String resolvedTitle = (title != null && !title.isBlank())
            ? title.trim()
            : stripExtension(file.getOriginalFilename());

        StudyMaterial material = StudyMaterial.builder()
            .subject(subject)
            .title(resolvedTitle)
            .content(extractedText.trim())
            .status(MaterialStatus.NOT_STARTED)
            .build();

        materialRepository.save(material);
        return MaterialResponse.from(material);
    }

    private String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try {
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    return new PDFTextStripper().getText(document);
                }
            }

            if (filename.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new ValidationException("Neuspešno čitanje fajla.");
        }

        throw new ValidationException("Podržani su samo PDF i TXT fajlovi.");
    }

    private String stripExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "Otpremljeno gradivo";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
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
