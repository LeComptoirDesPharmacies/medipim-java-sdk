package fr.lcdp.medipim.api.entities.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimPhotoFormats(String huge,
                                  String large,
                                  String medium) {}
