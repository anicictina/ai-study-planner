package com.anicictina.backend.ai;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanValidator {

    private static final int MIN_DURATION_MINUTES = 15;
    private static final int MAX_DURATION_MINUTES = 120;

    public StudyPlanValidationOutcome validate(
        List<RawProposedItem> rawItems,
        List<SubjectPlanningContext> subjects,
        List<AvailabilityWindow> availability,
        LocalDate today
    ) {
        Map<Long, SubjectPlanningContext> subjectsById = new HashMap<>();
        for (SubjectPlanningContext subject : subjects) {
            subjectsById.put(subject.subjectId(), subject);
        }

        List<ValidatedStudyPlanItem> validItems = new ArrayList<>();
        List<RejectedStudyPlanItem> rejectedItems = new ArrayList<>();
        Map<LocalDate, List<TimeRange>> acceptedRangesByDate = new HashMap<>();

        for (RawProposedItem raw : rawItems) {
            String rejectionReason = validateItem(raw, subjectsById, availability, today, acceptedRangesByDate);

            if (rejectionReason != null) {
                rejectedItems.add(new RejectedStudyPlanItem(raw, rejectionReason));
                continue;
            }

            LocalDate date = LocalDate.parse(raw.date());
            LocalTime startTime = LocalTime.parse(raw.startTime());
            LocalTime endTime = startTime.plusMinutes(raw.durationMinutes());

            acceptedRangesByDate.computeIfAbsent(date, d -> new ArrayList<>())
                .add(new TimeRange(startTime, endTime));

            validItems.add(new ValidatedStudyPlanItem(
                raw.subjectId(), date, startTime, raw.durationMinutes(), raw.topic()));
        }

        return new StudyPlanValidationOutcome(validItems, rejectedItems);
    }

    private String validateItem(
        RawProposedItem raw,
        Map<Long, SubjectPlanningContext> subjectsById,
        List<AvailabilityWindow> availability,
        LocalDate today,
        Map<LocalDate, List<TimeRange>> acceptedRangesByDate
    ) {
        if (raw.subjectId() == null || !subjectsById.containsKey(raw.subjectId())) {
            return "nepoznat predmet";
        }

        SubjectPlanningContext subject = subjectsById.get(raw.subjectId());

        LocalDate date;
        try {
            date = LocalDate.parse(raw.date());
        } catch (DateTimeException e) {
            return "neispravan format datuma";
        }

        LocalTime startTime;
        try {
            startTime = LocalTime.parse(raw.startTime());
        } catch (DateTimeException e) {
            return "neispravan format vremena";
        }

        if (raw.durationMinutes() == null
            || raw.durationMinutes() < MIN_DURATION_MINUTES
            || raw.durationMinutes() > MAX_DURATION_MINUTES) {
            return "trajanje mora biti između " + MIN_DURATION_MINUTES + " i " + MAX_DURATION_MINUTES + " minuta";
        }

        if (date.isBefore(today)) {
            return "datum je u prošlosti";
        }

        if (subject.horizonEnd() != null && date.isAfter(subject.horizonEnd())) {
            return "datum je posle krajnjeg roka za predmet (ispit)";
        }

        LocalTime endTime = startTime.plusMinutes(raw.durationMinutes());
        if (!endTime.isAfter(startTime)) {
            return "termin prelazi u naredni dan";
        }

        boolean withinAvailability = availability.stream().anyMatch(window ->
            window.dayOfWeek() == date.getDayOfWeek()
                && !startTime.isBefore(window.startTime())
                && !endTime.isAfter(window.endTime()));

        if (!withinAvailability) {
            return "van raspoloživog vremena korisnika";
        }

        List<TimeRange> existingRanges = acceptedRangesByDate.getOrDefault(date, List.of());
        TimeRange candidate = new TimeRange(startTime, endTime);
        boolean overlaps = existingRanges.stream().anyMatch(range -> range.overlaps(candidate));

        if (overlaps) {
            return "preklapa se sa drugim terminom u planu";
        }

        return null;
    }

    private record TimeRange(LocalTime start, LocalTime end) {
        boolean overlaps(TimeRange other) {
            return start.isBefore(other.end) && other.start.isBefore(end);
        }
    }
}
