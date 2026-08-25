package com.anicictina.backend.material;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeyDefinition {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String term;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;
}
