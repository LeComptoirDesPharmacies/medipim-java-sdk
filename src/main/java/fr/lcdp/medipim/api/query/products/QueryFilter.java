package fr.lcdp.medipim.api.query.products;

import fr.lcdp.medipim.api.query.QueryFilterHasContent;

import java.util.List;
import java.util.Locale;

public record QueryFilter(List<QueryFilter> or,
                          List<QueryFilter> and,
                          String acl13,
                          String cip13,
                          String ean,
                          String cipOrAcl7,

                          List<Long> media,
                          QueryFilterHasContent hasContent,
                          Long updatedSince,
                          Locale locale
                          ) {

    public static class QueryFilterBuilder {
        private List<QueryFilter> or = null;

        private List<QueryFilter> and = null;

        private String acl13 = null;
        private String cip13 = null;
        private String ean = null;
        private String cipOrAcl7 = null;

        private List<Long> media = null;
        private QueryFilterHasContent hasContent = null;

        private Long updatedSince = null;
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

        public QueryFilterBuilder acl13(String acl13) {
            this.acl13 = acl13;
            return this;
        }

        public QueryFilterBuilder cip13(String cip13) {
            this.cip13 = cip13;
            return this;
        }

        public QueryFilterBuilder ean(String ean) {
            this.ean = ean;
            return this;
        }

        public QueryFilterBuilder cipOrAcl7(String cipOrAcl7) {
            this.cipOrAcl7 = cipOrAcl7;
            return this;
        }

        public QueryFilterBuilder media(List<Long> media) {
            this.media = media;
            return this;
        }

        public QueryFilterBuilder hasContent(QueryFilterHasContent hasContent) {
            this.hasContent = hasContent;
            return this;
        }

        public QueryFilterBuilder updatedSince(Long updatedSince) {
            this.updatedSince = updatedSince;
            return this;
        }

        public QueryFilterBuilder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public QueryFilter build() {
            return new QueryFilter(or, and, acl13, cip13, ean, cipOrAcl7, media, hasContent, updatedSince, locale);
        }
    }
}