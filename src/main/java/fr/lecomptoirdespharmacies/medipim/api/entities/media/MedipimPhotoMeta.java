package fr.lecomptoirdespharmacies.medipim.api.entities.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimPhotoMeta(
        Instant createdAt,
        Instant updatedAt) {}
