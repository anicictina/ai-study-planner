package com.anicictina.backend.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuizValidator {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 6;

    public QuizValidationOutcome validate(List<RawQuizQuestion> rawQuestions) {
        List<RawQuizQuestion> validQuestions = new ArrayList<>();
        List<RejectedQuizQuestion> rejectedQuestions = new ArrayList<>();

        for (RawQuizQuestion question : rawQuestions) {
            String reason = validateQuestion(question);

            if (reason != null) {
                rejectedQuestions.add(new RejectedQuizQuestion(question, reason));
            } else {
                validQuestions.add(question);
            }
        }

        return new QuizValidationOutcome(validQuestions, rejectedQuestions);
    }

    private String validateQuestion(RawQuizQuestion question) {
        if (question.questionText() == null || question.questionText().isBlank()) {
            return "tekst pitanja je prazan";
        }

        if (question.options() == null
            || question.options().size() < MIN_OPTIONS
            || question.options().size() > MAX_OPTIONS) {
            return "broj ponuđenih odgovora mora biti između " + MIN_OPTIONS + " i " + MAX_OPTIONS;
        }

        if (question.options().stream().anyMatch(option -> option == null || option.isBlank())) {
            return "svi ponuđeni odgovori moraju imati tekst";
        }

        if (question.correctAnswerIndex() == null
            || question.correctAnswerIndex() < 0
            || question.correctAnswerIndex() >= question.options().size()) {
            return "tačan odgovor ne pokazuje na validnu opciju";
        }

        if (question.explanation() == null || question.explanation().isBlank()) {
            return "objašnjenje je prazno";
        }

        return null;
    }
}
