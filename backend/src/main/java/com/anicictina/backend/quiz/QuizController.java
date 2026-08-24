package com.anicictina.backend.quiz;

import com.anicictina.backend.quiz.dto.QuizAttemptSummaryResponse;
import com.anicictina.backend.quiz.dto.QuizGenerateRequest;
import com.anicictina.backend.quiz.dto.QuizResponse;
import com.anicictina.backend.quiz.dto.QuizResultResponse;
import com.anicictina.backend.quiz.dto.QuizSubmitRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generate(@Valid @RequestBody QuizGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.generate(request));
    }

    @GetMapping("/{id}")
    public QuizResponse findOne(@PathVariable Long id) {
        return quizService.findOne(id);
    }

    @PostMapping("/{id}/submit")
    public QuizResultResponse submit(@PathVariable Long id, @Valid @RequestBody QuizSubmitRequest request) {
        return quizService.submit(id, request);
    }

    @GetMapping("/attempts")
    public List<QuizAttemptSummaryResponse> getAttemptHistory() {
        return quizService.getAttemptHistory();
    }
}
