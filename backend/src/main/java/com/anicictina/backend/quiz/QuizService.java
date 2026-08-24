package com.anicictina.backend.quiz;

import com.anicictina.backend.ai.AIServiceException;
import com.anicictina.backend.ai.GeminiClient;
import com.anicictina.backend.ai.QuizPromptBuilder;
import com.anicictina.backend.ai.QuizValidationOutcome;
import com.anicictina.backend.ai.QuizValidator;
import com.anicictina.backend.ai.RawQuizQuestion;
import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.material.MaterialRepository;
import com.anicictina.backend.material.StudyMaterial;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.quiz.dto.QuestionAnswer;
import com.anicictina.backend.quiz.dto.QuestionResult;
import com.anicictina.backend.quiz.dto.QuizAttemptSummaryResponse;
import com.anicictina.backend.quiz.dto.QuizGenerateRequest;
import com.anicictina.backend.quiz.dto.QuizResponse;
import com.anicictina.backend.quiz.dto.QuizResultResponse;
import com.anicictina.backend.quiz.dto.QuizSubmitRequest;
import com.anicictina.backend.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final int DEFAULT_QUESTION_COUNT = 5;
    private static final int MIN_QUESTION_COUNT = 1;
    private static final int MAX_QUESTION_COUNT = 15;

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MaterialRepository materialRepository;
    private final CurrentUserProvider currentUserProvider;
    private final QuizPromptBuilder quizPromptBuilder;
    private final GeminiClient geminiClient;
    private final QuizValidator quizValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizResponse generate(QuizGenerateRequest request) {
        User user = currentUserProvider.getCurrentUser();
        StudyMaterial material = materialRepository.findByIdAndSubjectUserId(request.getMaterialId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        int questionCount = clamp(
            request.getQuestionCount() != null ? request.getQuestionCount() : DEFAULT_QUESTION_COUNT,
            MIN_QUESTION_COUNT,
            MAX_QUESTION_COUNT
        );

        String prompt = quizPromptBuilder.build(material.getTitle(), material.getContent(), questionCount);
        String rawJson = geminiClient.generateJson(prompt, quizPromptBuilder.buildResponseSchema());
        List<RawQuizQuestion> rawQuestions = parseRawQuestions(rawJson);

        QuizValidationOutcome outcome = quizValidator.validate(rawQuestions);

        if (outcome.validQuestions().isEmpty()) {
            throw new AIServiceException(
                "AI nije uspeo da generiše validna pitanja iz ovog gradiva. Pokušajte ponovo.");
        }

        Quiz quiz = new Quiz();
        quiz.setMaterial(material);
        quiz.setUser(user);

        for (RawQuizQuestion raw : outcome.validQuestions()) {
            QuizQuestion question = QuizQuestion.builder()
                .questionText(raw.questionText())
                .options(raw.options())
                .correctAnswerIndex(raw.correctAnswerIndex())
                .explanation(raw.explanation())
                .build();
            quiz.addQuestion(question);
        }

        quizRepository.save(quiz);
        return QuizResponse.from(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse findOne(Long id) {
        return QuizResponse.from(getOwnedQuiz(id));
    }

    @Transactional
    public QuizResultResponse submit(Long id, QuizSubmitRequest request) {
        Quiz quiz = getOwnedQuiz(id);
        User user = currentUserProvider.getCurrentUser();

        Map<Long, Integer> answersByQuestionId = new HashMap<>();
        for (QuestionAnswer answer : request.getAnswers()) {
            answersByQuestionId.put(answer.getQuestionId(), answer.getSelectedIndex());
        }

        List<QuestionResult> results = quiz.getQuestions().stream()
            .map(question -> {
                Integer selectedIndex = answersByQuestionId.get(question.getId());
                boolean correct = selectedIndex != null && selectedIndex.equals(question.getCorrectAnswerIndex());

                return QuestionResult.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .options(question.getOptions())
                    .selectedIndex(selectedIndex)
                    .correctAnswerIndex(question.getCorrectAnswerIndex())
                    .correct(correct)
                    .explanation(question.getExplanation())
                    .build();
            })
            .toList();

        int correctCount = (int) results.stream().filter(QuestionResult::correct).count();
        int totalCount = results.size();

        QuizAttempt attempt = QuizAttempt.builder()
            .quiz(quiz)
            .user(user)
            .correctCount(correctCount)
            .totalCount(totalCount)
            .build();
        quizAttemptRepository.save(attempt);

        return QuizResultResponse.builder()
            .quizId(quiz.getId())
            .correctCount(correctCount)
            .totalCount(totalCount)
            .results(results)
            .build();
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSummaryResponse> getAttemptHistory() {
        User user = currentUserProvider.getCurrentUser();
        return quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(user.getId()).stream()
            .map(QuizAttemptSummaryResponse::from)
            .toList();
    }

    private Quiz getOwnedQuiz(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return quizRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    private List<RawQuizQuestion> parseRawQuestions(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<RawQuizQuestion>>() { });
        } catch (IOException e) {
            throw new AIServiceException("AI servis je vratio neočekivan format odgovora.", e);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
