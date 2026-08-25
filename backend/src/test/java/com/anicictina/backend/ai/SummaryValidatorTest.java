package com.anicictina.backend.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SummaryValidatorTest {

    private SummaryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SummaryValidator();
    }

    @Test
    void acceptsWellFormedSummary() {
        RawMaterialSummary raw = new RawMaterialSummary(
            "Ovo je dovoljno dug sažetak gradiva o normalizaciji baza podataka.",
            List.of("Normalizacija", "3NF"),
            List.of(new RawKeyDefinition("3NF", "Treća normalna forma eliminiše tranzitivne zavisnosti.")),
            List.of("Šta je 3NF?")
        );

        RawMaterialSummary result = validator.validate(raw);

        assertEquals(2, result.keyTerms().size());
        assertEquals(1, result.keyDefinitions().size());
        assertEquals(1, result.practiceQuestions().size());
    }

    @Test
    void rejectsBlankSummaryText() {
        RawMaterialSummary raw = new RawMaterialSummary("  ", List.of("Pojam"), List.of(), List.of());

        assertThrows(AIServiceException.class, () -> validator.validate(raw));
    }

    @Test
    void rejectsTooShortSummaryText() {
        RawMaterialSummary raw = new RawMaterialSummary("Kratko.", List.of("Pojam"), List.of(), List.of());

        assertThrows(AIServiceException.class, () -> validator.validate(raw));
    }

    @Test
    void rejectsWhenAllSupportingListsAreEmpty() {
        RawMaterialSummary raw = new RawMaterialSummary(
            "Ovo je dovoljno dug sažetak, ali bez ijednog pojma, definicije ili pitanja.",
            List.of(),
            List.of(),
            List.of()
        );

        assertThrows(AIServiceException.class, () -> validator.validate(raw));
    }

    @Test
    void filtersOutBlankKeyTermsAndQuestions() {
        RawMaterialSummary raw = new RawMaterialSummary(
            "Ovo je dovoljno dug sažetak gradiva o normalizaciji baza podataka.",
            Arrays.asList("Normalizacija", "  ", ""),
            List.of(),
            Arrays.asList("Pitanje 1?", "")
        );

        RawMaterialSummary result = validator.validate(raw);

        assertEquals(1, result.keyTerms().size());
        assertEquals(1, result.practiceQuestions().size());
    }

    @Test
    void filtersOutDefinitionsWithBlankTermOrDefinition() {
        RawMaterialSummary raw = new RawMaterialSummary(
            "Ovo je dovoljno dug sažetak gradiva o normalizaciji baza podataka.",
            List.of("Normalizacija"),
            Arrays.asList(
                new RawKeyDefinition("3NF", "Treća normalna forma."),
                new RawKeyDefinition("", "Definicija bez pojma."),
                new RawKeyDefinition("Pojam bez definicije", "")
            ),
            List.of()
        );

        RawMaterialSummary result = validator.validate(raw);

        assertEquals(1, result.keyDefinitions().size());
        assertTrue(result.keyDefinitions().get(0).term().equals("3NF"));
    }
}
