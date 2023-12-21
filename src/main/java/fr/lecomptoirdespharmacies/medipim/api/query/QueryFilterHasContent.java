package fr.lecomptoirdespharmacies.medipim.api.query;

public record QueryFilterHasContent(String flag,
                                    String locale
                          ) {

    public static class QueryFilterHasContentBuilder {
        private String flag = null;
        private String locale = null;

        public QueryFilterHasContentBuilder() {}

        public QueryFilterHasContentBuilder flag(String flag) {
            this.flag = flag;
            return this;
        }

        public QueryFilterHasContentBuilder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public QueryFilterHasContent build() {
            return new QueryFilterHasContent(flag, locale);
        }
    }
}