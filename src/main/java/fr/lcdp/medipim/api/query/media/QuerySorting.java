package fr.lcdp.medipim.api.query.media;

import fr.lcdp.medipim.api.query.SortingValue;

public record QuerySorting(SortingValue id,
                           SortingValue createdAt,
                           SortingValue touchedAt) {

    public static class QuerySortingBuilder {
        private SortingValue id = null;
        private SortingValue createdAt = null;
        private SortingValue touchedAt = null;

        public QuerySortingBuilder() {}

        public QuerySortingBuilder id(SortingValue id) {
            this.id = id;
            return this;
        }

        public QuerySortingBuilder createdAt(SortingValue createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public QuerySortingBuilder touchedAt(SortingValue touchedAt) {
            this.touchedAt = touchedAt;
            return this;
        }

        public QuerySorting build() {
            return new QuerySorting(id, createdAt, touchedAt);
        }
    }
}