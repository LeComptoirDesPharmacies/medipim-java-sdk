package fr.lcdp.medipim.api.entities.products;

import fr.lcdp.medipim.api.entities.media.MedipimPhoto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedipimProduct(String id,
                             BigDecimal weight,
                             String cipOrAcl7,
                             String cip13,
                             String acl13,
                             List<String> ean,
                             List<MedipimPhoto> photos,
                             MedipimProductMeta meta) {}

