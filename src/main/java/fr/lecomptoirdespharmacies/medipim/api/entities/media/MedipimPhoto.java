package fr.lecomptoirdespharmacies.medipim.api.entities.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimPhoto(Long id,
                           List<Locale> locales,
                           MedipimPhotoType photoType,
                           MedipimPhotoFormats formats,
                           List<String> visibleSides,
                           MedipimPhotoMeta meta) {}
