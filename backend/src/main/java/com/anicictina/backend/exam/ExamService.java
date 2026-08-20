package com.anicictina.backend.exam;

import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.exam.dto.ExamRequest;
import com.anicictina.backend.exam.dto.ExamResponse;
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
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<ExamResponse> findAll(Long subjectId) {
        User user = currentUserProvider.getCurrentUser();
        return examRepository.findAllForUserOrderedByDateAndPriority(user.getId()).stream()
            .filter(exam -> subjectId == null || exam.getSubject().getId().equals(subjectId))
            .map(ExamResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExamResponse findOne(Long id) {
        return ExamResponse.from(getOwnedExam(id));
    }

    @Transactional
    public ExamResponse create(ExamRequest request) {
        Subject subject = getOwnedSubject(request.getSubjectId());

        Exam exam = Exam.builder()
            .subject(subject)
            .examDate(request.getExamDate())
            .examTime(request.getExamTime())
            .location(request.getLocation())
            .status(request.getStatus() != null ? request.getStatus() : ExamStatus.PLANNED)
            .build();

        examRepository.save(exam);
        return ExamResponse.from(exam);
    }

    @Transactional
    public ExamResponse update(Long id, ExamRequest request) {
        Exam exam = getOwnedExam(id);

        if (!exam.getSubject().getId().equals(request.getSubjectId())) {
            exam.setSubject(getOwnedSubject(request.getSubjectId()));
        }

        exam.setExamDate(request.getExamDate());
        exam.setExamTime(request.getExamTime());
        exam.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            exam.setStatus(request.getStatus());
        }

        return ExamResponse.from(exam);
    }

    @Transactional
    public void delete(Long id) {
        Exam exam = getOwnedExam(id);
        examRepository.delete(exam);
    }

    private Exam getOwnedExam(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return examRepository.findByIdAndSubjectUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
    }

    private Subject getOwnedSubject(Long subjectId) {
        User user = currentUserProvider.getCurrentUser();
        return subjectRepository.findByIdAndUserId(subjectId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }
}
