package fr.lecomptoirdespharmacies.medipim.api.query.products;

import fr.lecomptoirdespharmacies.medipim.api.query.SortingValue;

public record QuerySorting(SortingValue id, 
                           SortingValue name, 
                           SortingValue createdAt, 
                           SortingValue touchedAt,
                           SortingValue statusTouchedAt,
                           SortingValue status) {

    public static class QuerySortingBuilder {
        private SortingValue id = null;
        private SortingValue name = null;
        private SortingValue createdAt = null;
        private SortingValue touchedAt = null;
        private SortingValue statusTouchedAt = null;
        private SortingValue status = null;

        public QuerySortingBuilder() {}

        public QuerySortingBuilder id(SortingValue id) {
            this.id = id;
            return this;
        }

        public QuerySortingBuilder name(SortingValue name) {
            this.name = name;
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

        public QuerySortingBuilder statusTouchedAt(SortingValue statusTouchedAt) {
            this.statusTouchedAt = statusTouchedAt;
            return this;
        }

        public QuerySortingBuilder status(SortingValue status) {
            this.status = status;
            return this;
        }

        public QuerySorting build() {
            return new QuerySorting(id, name, createdAt, touchedAt, statusTouchedAt, status);
        }
    }
}