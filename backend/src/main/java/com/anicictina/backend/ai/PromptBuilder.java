package com.anicictina.backend.ai;

import com.anicictina.backend.user.PreferredTime;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    private static final Locale SERBIAN = Locale.forLanguageTag("sr");

    public String build(
        List<SubjectPlanningContext> subjects,
        List<AvailabilityWindow> availability,
        PreferredTime preferredTime,
        LocalDate today
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("Ti si asistent za planiranje učenja studenata. Na osnovu podataka ispod, predloži ")
            .append("konkretan raspored sesija učenja.\n\n");

        sb.append("DANAŠNJI DATUM: ").append(today).append("\n\n");

        sb.append("PREFERIRANO VREME UČENJA: ")
            .append(preferredTime != null ? translatePreferredTime(preferredTime) : "nije određeno")
            .append("\n\n");

        sb.append("RASPOLOŽIVI TERMINI STUDENTA (van ovih termina se NE SME planirati ništa):\n");
        if (availability.isEmpty()) {
            sb.append("(nema definisanih termina)\n");
        } else {
            for (AvailabilityWindow window : availability) {
                sb.append("- ")
                    .append(window.dayOfWeek().getDisplayName(TextStyle.FULL, SERBIAN))
                    .append(": ")
                    .append(window.startTime())
                    .append(" - ")
                    .append(window.endTime())
                    .append("\n");
            }
        }
        sb.append("\n");

        sb.append("PREDMETI ZA PLANIRANJE:\n");
        for (SubjectPlanningContext subject : subjects) {
            sb.append("- id=").append(subject.subjectId())
                .append(", naziv=\"").append(subject.subjectName()).append("\"")
                .append(", težina=").append(subject.difficulty())
                .append(", prioritet=").append(subject.priority())
                .append(", trenutno znanje=").append(subject.knowledgePercent()).append("%")
                .append(", planiraj najkasnije do=").append(subject.horizonEnd());

            if (!subject.completedTopics().isEmpty()) {
                sb.append(", već obrađene teme=[")
                    .append(String.join(", ", subject.completedTopics()))
                    .append("]");
            }
            sb.append("\n");
        }
        sb.append("\n");

        sb.append("PRAVILA (obavezno poštovati):\n")
            .append("1. Svaka sesija mora biti zakazana STROGO unutar raspoloživih termina studenta ")
            .append("(isti dan u nedelji i unutar vremenskog opsega).\n")
            .append("2. Nijedna sesija ne sme biti zakazana pre današnjeg datuma.\n")
            .append("3. Nijedna sesija za dati predmet ne sme biti zakazana posle njegovog krajnjeg datuma ")
            .append("(\"planiraj najkasnije do\").\n")
            .append("4. Trajanje sesije mora biti pozitivno, između 15 i 120 minuta.\n")
            .append("5. Sesije se ne smeju vremenski preklapati.\n")
            .append("6. Predmetima sa nižim trenutnim znanjem i višim prioritetom/težinom posveti više vremena.\n")
            .append("7. Izbegavaj ponavljanje tema koje su već obrađene, osim kratkog ponavljanja pred kraj.\n")
            .append("8. Za svaku sesiju navedi konkretnu, korisnu temu (topic) vezanu za predmet.\n\n");

        sb.append("Vrati isključivo JSON niz objekata sa poljima: subjectId (broj), date (format YYYY-MM-DD), ")
            .append("startTime (format HH:mm), durationMinutes (broj), topic (kratak tekst). ")
            .append("Ne dodaji nikakav tekst van JSON niza.");

        return sb.toString();
    }

    private String translatePreferredTime(PreferredTime preferredTime) {
        return switch (preferredTime) {
            case MORNING -> "jutro";
            case AFTERNOON -> "popodne";
            case EVENING -> "veče";
        };
    }

    public Map<String, Object> buildResponseSchema() {
        Map<String, Object> itemProperties = Map.of(
            "subjectId", Map.of("type", "INTEGER"),
            "date", Map.of("type", "STRING"),
            "startTime", Map.of("type", "STRING"),
            "durationMinutes", Map.of("type", "INTEGER"),
            "topic", Map.of("type", "STRING")
        );

        Map<String, Object> itemSchema = Map.of(
            "type", "OBJECT",
            "properties", itemProperties,
            "required", List.of("subjectId", "date", "startTime", "durationMinutes", "topic")
        );

        return Map.of(
            "type", "ARRAY",
            "items", itemSchema
        );
    }
}
