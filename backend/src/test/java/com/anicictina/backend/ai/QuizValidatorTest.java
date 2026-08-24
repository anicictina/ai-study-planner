package com.anicictina.backend.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuizValidatorTest {

    private QuizValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuizValidator();
    }

    private RawQuizQuestion question(String text, List<String> options, Integer correctIndex, String explanation) {
        return new RawQuizQuestion(text, options, correctIndex, explanation);
    }

    @Test
    void acceptsWellFormedQuestion() {
        RawQuizQuestion valid = question(
            "Šta je normalizacija?",
            List.of("Smanjenje redundanse", "Brisanje tabela", "Uvećanje redundanse", "Šifrovanje"),
            0,
            "Normalizacija smanjuje redundansu podataka."
        );

        QuizValidationOutcome outcome = validator.validate(List.of(valid));

        assertEquals(1, outcome.validQuestions().size());
        assertTrue(outcome.rejectedQuestions().isEmpty());
    }

    @Test
    void rejectsBlankQuestionText() {
        RawQuizQuestion invalid = question("  ", List.of("A", "B"), 0, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
        assertTrue(outcome.rejectedQuestions().get(0).reason().contains("tekst pitanja"));
    }

    @Test
    void rejectsTooFewOptions() {
        RawQuizQuestion invalid = question("Pitanje?", List.of("Samo jedna opcija"), 0, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
        assertTrue(outcome.rejectedQuestions().get(0).reason().contains("broj ponuđenih odgovora"));
    }

    @Test
    void rejectsTooManyOptions() {
        RawQuizQuestion invalid = question(
            "Pitanje?", Arrays.asList("A", "B", "C", "D", "E", "F", "G"), 0, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
    }

    @Test
    void rejectsBlankOption() {
        RawQuizQuestion invalid = question("Pitanje?", Arrays.asList("A", "", "C", "D"), 0, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
        assertTrue(outcome.rejectedQuestions().get(0).reason().contains("ponuđeni odgovori"));
    }

    @Test
    void rejectsCorrectAnswerIndexOutOfBounds() {
        RawQuizQuestion invalid = question("Pitanje?", List.of("A", "B", "C", "D"), 4, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
        assertTrue(outcome.rejectedQuestions().get(0).reason().contains("tačan odgovor"));
    }

    @Test
    void rejectsNegativeCorrectAnswerIndex() {
        RawQuizQuestion invalid = question("Pitanje?", List.of("A", "B", "C", "D"), -1, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
    }

    @Test
    void rejectsMissingExplanation() {
        RawQuizQuestion invalid = question("Pitanje?", List.of("A", "B", "C", "D"), 0, "");

        QuizValidationOutcome outcome = validator.validate(List.of(invalid));

        assertTrue(outcome.validQuestions().isEmpty());
        assertEquals(1, outcome.rejectedQuestions().size());
        assertTrue(outcome.rejectedQuestions().get(0).reason().contains("objašnjenje"));
    }

    @Test
    void keepsValidQuestionsAndRejectsInvalidOnesInSameBatch() {
        RawQuizQuestion valid = question("Pitanje 1?", List.of("A", "B", "C", "D"), 1, "Jer je B tačno.");
        RawQuizQuestion invalid = question("Pitanje 2?", List.of("A"), 0, "Objašnjenje");

        QuizValidationOutcome outcome = validator.validate(List.of(valid, invalid));

        assertEquals(1, outcome.validQuestions().size());
        assertEquals(1, outcome.rejectedQuestions().size());
    }
}
