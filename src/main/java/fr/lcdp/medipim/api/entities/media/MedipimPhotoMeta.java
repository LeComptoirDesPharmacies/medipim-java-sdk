package fr.lcdp.medipim.api.entities.media;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimPhotoMeta(
        Instant createdAt,
        Instant updatedAt) {}
