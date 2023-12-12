package fr.lcdp.medipim.api.query.media;

import fr.lcdp.medipim.api.entities.media.MedipimMediaType;

import java.util.List;
import java.util.Locale;

public record QueryFilter(List<QueryFilter> or,
                          List<QueryFilter> and,
                          MedipimMediaType type,
                          Long touchedAt,
                          Locale locale
                          ) {

    public static class QueryFilterBuilder {
        private List<QueryFilter> or = null;

        private List<QueryFilter> and = null;

        private MedipimMediaType type = null;

        private Long touchedAt = null;

        private Locale locale = null;

        public QueryFilterBuilder() {}

        public QueryFilterBuilder or(List<QueryFilter> or) {
            this.or = or;
            return this;
        }

        public QueryFilterBuilder and(List<QueryFilter> and) {
            this.and = and;
            return this;
        }

        public QueryFilterBuilder type(MedipimMediaType type) {
            this.type = type;
            return this;
        }

        public QueryFilterBuilder touchedAt(Long touchedAt) {
            this.touchedAt = touchedAt;
            return this;
        }

        public QueryFilterBuilder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public QueryFilter build() {
            return new QueryFilter(or, and, type, touchedAt, locale);
        }
    }
}