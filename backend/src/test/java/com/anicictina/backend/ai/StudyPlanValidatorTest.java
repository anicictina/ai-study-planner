package com.anicictina.backend.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anicictina.backend.subject.Level;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudyPlanValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 2);
    private static final DayOfWeek TODAY_DOW = TODAY.getDayOfWeek();

    private StudyPlanValidator validator;
    private List<SubjectPlanningContext> subjects;
    private List<AvailabilityWindow> availability;

    @BeforeEach
    void setUp() {
        validator = new StudyPlanValidator();

        subjects = List.of(
            new SubjectPlanningContext(
                1L, "Baze podataka", Level.HIGH, Level.HIGH, 40, TODAY.plusDays(10), List.of())
        );

        availability = List.of(
            new AvailabilityWindow(TODAY_DOW, LocalTime.of(17, 0), LocalTime.of(20, 0))
        );
    }

    private RawProposedItem item(Long subjectId, String date, String startTime, Integer duration, String topic) {
        return new RawProposedItem(subjectId, date, startTime, duration, topic);
    }

    @Test
    void acceptsItemWithinAvailabilityAndBeforeExam() {
        RawProposedItem valid = item(1L, TODAY.toString(), "17:00", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome = validator.validate(List.of(valid), subjects, availability, TODAY);

        assertEquals(1, outcome.validItems().size());
        assertTrue(outcome.rejectedItems().isEmpty());
    }

    @Test
    void rejectsItemOutsideAvailabilityTimeRange() {
        RawProposedItem tooLate = item(1L, TODAY.toString(), "21:00", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome = validator.validate(List.of(tooLate), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(1, outcome.rejectedItems().size());
    }

    @Test
    void rejectsItemOnDayWithNoAvailabilityWindow() {
        DayOfWeek otherDay = TODAY_DOW.plus(3);
        LocalDate dateOnOtherDay = TODAY.plusDays(3);
        assertEquals(otherDay, dateOnOtherDay.getDayOfWeek());

        RawProposedItem wrongDay = item(1L, dateOnOtherDay.toString(), "17:00", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome = validator.validate(List.of(wrongDay), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(1, outcome.rejectedItems().size());
    }

    @Test
    void rejectsItemAfterSubjectExamDate() {
        RawProposedItem afterExam = item(1L, TODAY.plusDays(11).toString(), "17:00", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome = validator.validate(List.of(afterExam), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(1, outcome.rejectedItems().size());
        assertTrue(outcome.rejectedItems().get(0).reason().contains("posle krajnjeg roka"));
    }

    @Test
    void rejectsItemInThePast() {
        RawProposedItem pastItem = item(1L, TODAY.minusDays(1).toString(), "17:00", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome = validator.validate(List.of(pastItem), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(1, outcome.rejectedItems().size());
        assertTrue(outcome.rejectedItems().get(0).reason().contains("prošlosti"));
    }

    @Test
    void rejectsSecondOverlappingItemOnSameDay() {
        RawProposedItem first = item(1L, TODAY.toString(), "17:00", 90, "SQL osnove");
        RawProposedItem overlapping = item(1L, TODAY.toString(), "17:30", 60, "JOIN operacije");

        StudyPlanValidationOutcome outcome =
            validator.validate(List.of(first, overlapping), subjects, availability, TODAY);

        assertEquals(1, outcome.validItems().size());
        assertEquals(1, outcome.rejectedItems().size());
        assertTrue(outcome.rejectedItems().get(0).reason().contains("preklapa"));
    }

    @Test
    void acceptsBackToBackNonOverlappingItems() {
        RawProposedItem first = item(1L, TODAY.toString(), "17:00", 60, "SQL osnove");
        RawProposedItem backToBack = item(1L, TODAY.toString(), "18:00", 60, "JOIN operacije");

        StudyPlanValidationOutcome outcome =
            validator.validate(List.of(first, backToBack), subjects, availability, TODAY);

        assertEquals(2, outcome.validItems().size());
        assertTrue(outcome.rejectedItems().isEmpty());
    }

    @Test
    void rejectsDurationOutsideAllowedRange() {
        RawProposedItem tooShort = item(1L, TODAY.toString(), "17:00", 5, "SQL osnove");
        RawProposedItem tooLong = item(1L, TODAY.toString(), "17:00", 300, "SQL osnove");

        StudyPlanValidationOutcome outcome =
            validator.validate(List.of(tooShort, tooLong), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(2, outcome.rejectedItems().size());
    }

    @Test
    void rejectsUnknownSubjectId() {
        RawProposedItem unknownSubject = item(999L, TODAY.toString(), "17:00", 60, "Nepoznato");

        StudyPlanValidationOutcome outcome =
            validator.validate(List.of(unknownSubject), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(1, outcome.rejectedItems().size());
        assertTrue(outcome.rejectedItems().get(0).reason().contains("nepoznat predmet"));
    }

    @Test
    void rejectsMalformedDateAndTimeGracefully() {
        RawProposedItem badDate = item(1L, "not-a-date", "17:00", 60, "SQL osnove");
        RawProposedItem badTime = item(1L, TODAY.toString(), "not-a-time", 60, "SQL osnove");

        StudyPlanValidationOutcome outcome =
            validator.validate(List.of(badDate, badTime), subjects, availability, TODAY);

        assertTrue(outcome.validItems().isEmpty());
        assertEquals(2, outcome.rejectedItems().size());
    }
}
