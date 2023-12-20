package fr.lecomptoirdespharmacies.medipim.api.entities.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimProductMeta(
        Instant createdAt,
        Instant updatedAt) {}

