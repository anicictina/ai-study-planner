package com.anicictina.backend.ai;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SummaryPromptBuilder {

    public String build(String materialTitle, String materialContent) {
        StringBuilder sb = new StringBuilder();

        sb.append("Ti si asistent koji pravi sažetak gradiva za studenta ISKLJUČIVO na osnovu ")
            .append("teksta gradiva ispod. NE koristi svoje opšte znanje niti dodaji činjenice koje ")
            .append("nisu direktno sadržane ili izvedene iz ovog teksta.\n\n");

        sb.append("NASLOV GRADIVA: ").append(materialTitle).append("\n\n");

        sb.append("TEKST GRADIVA:\n").append(materialContent).append("\n\n");

        sb.append("ZADATAK:\n")
            .append("Napravi sažetak ovog gradiva koji sadrži:\n")
            .append("1. Kratak sažetak (nekoliko rečenica ili pasusa) koji prenosi glavne ideje teksta.\n")
            .append("2. Listu ključnih pojmova pomenutih u tekstu.\n")
            .append("3. Listu najvažnijih definicija iz teksta, svaka sa pojmom i njegovom definicijom.\n")
            .append("4. Listu potencijalnih pitanja za ispit zasnovanih na ovom gradivu.\n\n");

        sb.append("Vrati isključivo JSON objekat sa poljima: summaryText (string), ")
            .append("keyTerms (niz stringova), keyDefinitions (niz objekata sa poljima term i definition), ")
            .append("practiceQuestions (niz stringova). Ne dodaji nikakav tekst van JSON objekta.");

        return sb.toString();
    }

    public Map<String, Object> buildResponseSchema() {
        Map<String, Object> keyDefinitionSchema = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "term", Map.of("type", "STRING"),
                "definition", Map.of("type", "STRING")
            ),
            "required", List.of("term", "definition")
        );

        Map<String, Object> properties = Map.of(
            "summaryText", Map.of("type", "STRING"),
            "keyTerms", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
            "keyDefinitions", Map.of("type", "ARRAY", "items", keyDefinitionSchema),
            "practiceQuestions", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))
        );

        return Map.of(
            "type", "OBJECT",
            "properties", properties,
            "required", List.of("summaryText", "keyTerms", "keyDefinitions", "practiceQuestions")
        );
    }
}
