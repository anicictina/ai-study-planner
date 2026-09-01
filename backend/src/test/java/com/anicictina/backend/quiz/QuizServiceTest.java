package com.anicictina.backend.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.ai.AIServiceException;
import com.anicictina.backend.ai.GeminiClient;
import com.anicictina.backend.ai.QuizPromptBuilder;
import com.anicictina.backend.ai.QuizValidationOutcome;
import com.anicictina.backend.ai.QuizValidator;
import com.anicictina.backend.ai.RawQuizQuestion;
import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.material.MaterialRepository;
import com.anicictina.backend.material.StudyMaterial;
import com.anicictina.backend.quiz.dto.QuestionAnswer;
import com.anicictina.backend.quiz.dto.QuizGenerateRequest;
import com.anicictina.backend.quiz.dto.QuizSubmitRequest;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private QuizPromptBuilder quizPromptBuilder;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private QuizValidator quizValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuizService quizService;

    private User currentUser;
    private StudyMaterial ownedMaterial;

    @BeforeEach
    void setUp() {
        quizService = new QuizService(
            quizRepository, quizAttemptRepository, materialRepository,
            currentUserProvider, quizPromptBuilder, geminiClient, quizValidator, objectMapper);

        currentUser = User.builder().id(1L).email("owner@example.com").build();
        Subject subject = Subject.builder().id(3L).user(currentUser).name("Baze podataka").color("#000").build();
        ownedMaterial = StudyMaterial.builder().id(4L).subject(subject).title("Predavanje").content("sadrzaj").build();
    }

    private RawQuizQuestion validQuestion() {
        return new RawQuizQuestion("Pitanje?", List.of("A", "B", "C", "D"), 1, "Objasnjenje");
    }

    @Test
    void generateThrowsWhenMaterialBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(4L, 1L)).thenReturn(Optional.empty());

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setMaterialId(4L);

        assertThrows(ResourceNotFoundException.class, () -> quizService.generate(request));
    }

    @Test
    void generateThrowsWhenValidatorRejectsAllQuestions() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(4L, 1L)).thenReturn(Optional.of(ownedMaterial));
        when(quizPromptBuilder.build(anyString(), anyString(), anyInt())).thenReturn("prompt");
        when(quizPromptBuilder.buildResponseSchema()).thenReturn(java.util.Map.of());
        when(geminiClient.generateJson(anyString(), anyMap())).thenReturn("[]");
        when(quizValidator.validate(any())).thenReturn(new QuizValidationOutcome(List.of(), List.of()));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setMaterialId(4L);

        assertThrows(AIServiceException.class, () -> quizService.generate(request));
        verify(quizRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void generateClampsQuestionCountAboveMaximum() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(4L, 1L)).thenReturn(Optional.of(ownedMaterial));
        when(quizPromptBuilder.build(anyString(), anyString(), anyInt())).thenReturn("prompt");
        when(quizPromptBuilder.buildResponseSchema()).thenReturn(java.util.Map.of());
        when(geminiClient.generateJson(anyString(), anyMap())).thenReturn("[]");
        when(quizValidator.validate(any())).thenReturn(new QuizValidationOutcome(List.of(validQuestion()), List.of()));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setMaterialId(4L);
        request.setQuestionCount(999);

        quizService.generate(request);

        verify(quizPromptBuilder).build(anyString(), anyString(), org.mockito.ArgumentMatchers.eq(15));
    }

    @Test
    void generatePersistsQuizWithOnlyValidQuestions() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(4L, 1L)).thenReturn(Optional.of(ownedMaterial));
        when(quizPromptBuilder.build(anyString(), anyString(), anyInt())).thenReturn("prompt");
        when(quizPromptBuilder.buildResponseSchema()).thenReturn(java.util.Map.of());
        when(geminiClient.generateJson(anyString(), anyMap())).thenReturn("[]");
        when(quizValidator.validate(any())).thenReturn(new QuizValidationOutcome(List.of(validQuestion()), List.of()));

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setMaterialId(4L);

        var response = quizService.generate(request);

        assertEquals(1, response.questions().size());
    }

    @Test
    void findOneThrowsWhenQuizBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(quizRepository.findByIdAndUserId(6L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.findOne(6L));
    }

    @Test
    void submitComputesCorrectAndIncorrectAnswers() {
        Quiz quiz = new Quiz();
        quiz.setUser(currentUser);
        quiz.setMaterial(ownedMaterial);
        QuizQuestion q1 = QuizQuestion.builder()
            .id(101L).questionText("P1").options(List.of("A", "B")).correctAnswerIndex(0).explanation("e").build();
        QuizQuestion q2 = QuizQuestion.builder()
            .id(102L).questionText("P2").options(List.of("A", "B")).correctAnswerIndex(1).explanation("e").build();
        quiz.addQuestion(q1);
        quiz.addQuestion(q2);

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(quizRepository.findByIdAndUserId(6L, 1L)).thenReturn(Optional.of(quiz));

        QuestionAnswer correctAnswer = new QuestionAnswer();
        correctAnswer.setQuestionId(101L);
        correctAnswer.setSelectedIndex(0);
        QuestionAnswer wrongAnswer = new QuestionAnswer();
        wrongAnswer.setQuestionId(102L);
        wrongAnswer.setSelectedIndex(0);

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(List.of(correctAnswer, wrongAnswer));

        var result = quizService.submit(6L, request);

        assertEquals(1, result.correctCount());
        assertEquals(2, result.totalCount());

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getCorrectCount());
    }

    @Test
    void getAttemptHistoryQueriesOnlyCurrentUsersAttempts() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of());

        quizService.getAttemptHistory();

        verify(quizAttemptRepository).findByUserIdOrderByAttemptedAtDesc(1L);
    }
}
