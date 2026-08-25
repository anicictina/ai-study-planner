package com.anicictina.backend.material;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "material_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private StudyMaterial material;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @ElementCollection
    @CollectionTable(name = "material_summary_key_terms", joinColumns = @JoinColumn(name = "material_summary_id"))
    @OrderColumn(name = "term_order")
    @Column(name = "term", nullable = false, columnDefinition = "TEXT")
    private List<String> keyTerms;

    @ElementCollection
    @CollectionTable(name = "material_summary_definitions", joinColumns = @JoinColumn(name = "material_summary_id"))
    @OrderColumn(name = "definition_order")
    private List<KeyDefinition> keyDefinitions;

    @ElementCollection
    @CollectionTable(name = "material_summary_questions", joinColumns = @JoinColumn(name = "material_summary_id"))
    @OrderColumn(name = "question_order")
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private List<String> practiceQuestions;

    @Column(nullable = false)
    private Instant generatedAt;

    @PrePersist
    void prePersist() {
        if (generatedAt == null) {
            generatedAt = Instant.now();
        }
    }
}
