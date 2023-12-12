package fr.lcdp.medipim.api.entities.media;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MedipimPhotoType {
    @JsonProperty("packshot")
    PACKSHOT,
    @JsonProperty("productshot")
    PRODUCTSHOT,
    @JsonProperty("pillshot")
    PILLSHOT,
    @JsonProperty("lifestyle_image")
    LIFESTYLE_IMAGE
}
