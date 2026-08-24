package com.anicictina.backend.ai;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QuizPromptBuilder {

    public String build(String materialTitle, String materialContent, int questionCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("Ti si asistent koji pravi kviz za proveru znanja studenta ISKLJUČIVO na osnovu ")
            .append("teksta gradiva ispod. NE koristi svoje opšte znanje niti dodaji činjenice koje ")
            .append("nisu direktno sadržane ili izvedene iz ovog teksta.\n\n");

        sb.append("NASLOV GRADIVA: ").append(materialTitle).append("\n\n");

        sb.append("TEKST GRADIVA:\n").append(materialContent).append("\n\n");

        sb.append("ZADATAK:\n")
            .append("Napravi tačno ").append(questionCount).append(" pitanja sa višestrukim izborom ")
            .append("na osnovu teksta gradiva iznad.\n\n");

        sb.append("PRAVILA (obavezno poštovati):\n")
            .append("1. Svako pitanje mora biti odgovorivo isključivo na osnovu datog teksta.\n")
            .append("2. Svako pitanje ima tačno 4 ponuđena odgovora, od kojih je tačno jedan ispravan.\n")
            .append("3. Ponuđeni odgovori moraju biti međusobno različiti i verodostojni.\n")
            .append("4. correctAnswerIndex je indeks (0-3) tačnog odgovora u nizu options.\n")
            .append("5. Za svako pitanje napiši kratko objašnjenje zašto je tačan odgovor tačan, ")
            .append("pozivajući se na tekst gradiva.\n")
            .append("6. Pitanja treba da pokrivaju različite delove teksta, ne samo jedan pasus.\n\n");

        sb.append("Vrati isključivo JSON niz objekata sa poljima: questionText (tekst pitanja), ")
            .append("options (niz od tačno 4 teksta), correctAnswerIndex (broj 0-3), ")
            .append("explanation (kratko objašnjenje). Ne dodaji nikakav tekst van JSON niza.");

        return sb.toString();
    }

    public Map<String, Object> buildResponseSchema() {
        Map<String, Object> questionProperties = Map.of(
            "questionText", Map.of("type", "STRING"),
            "options", Map.of(
                "type", "ARRAY",
                "items", Map.of("type", "STRING")
            ),
            "correctAnswerIndex", Map.of("type", "INTEGER"),
            "explanation", Map.of("type", "STRING")
        );

        Map<String, Object> questionSchema = Map.of(
            "type", "OBJECT",
            "properties", questionProperties,
            "required", List.of("questionText", "options", "correctAnswerIndex", "explanation")
        );

        return Map.of(
            "type", "ARRAY",
            "items", questionSchema
        );
    }
}
