package com.anicictina.backend.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SummaryValidator {

    private static final int MIN_SUMMARY_LENGTH = 20;

    public RawMaterialSummary validate(RawMaterialSummary raw) {
        if (raw.summaryText() == null || raw.summaryText().isBlank()
            || raw.summaryText().trim().length() < MIN_SUMMARY_LENGTH) {
            throw new AIServiceException("AI nije uspeo da generiše sažetak za ovo gradivo. Pokušajte ponovo.");
        }

        List<String> keyTerms = filterBlanks(raw.keyTerms());
        List<String> practiceQuestions = filterBlanks(raw.practiceQuestions());
        List<RawKeyDefinition> keyDefinitions = raw.keyDefinitions() == null
            ? List.of()
            : raw.keyDefinitions().stream()
                .filter(def -> def != null
                    && def.term() != null && !def.term().isBlank()
                    && def.definition() != null && !def.definition().isBlank())
                .toList();

        if (keyTerms.isEmpty() && keyDefinitions.isEmpty() && practiceQuestions.isEmpty()) {
            throw new AIServiceException("AI nije uspeo da generiše sažetak za ovo gradivo. Pokušajte ponovo.");
        }

        return new RawMaterialSummary(raw.summaryText().trim(), keyTerms, keyDefinitions, practiceQuestions);
    }

    private List<String> filterBlanks(List<String> values) {
        return values == null
            ? List.of()
            : values.stream().filter(value -> value != null && !value.isBlank()).toList();
    }
}
