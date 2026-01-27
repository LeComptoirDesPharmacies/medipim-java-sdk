package fr.lecomptoirdespharmacies.medipim.api.query;

public record QueryPage(int no, PageSize size) {

    public enum PageSize {
        SIZE_10(10),
        SIZE_50(50),
        SIZE_100(100),
        SIZE_250(250);

        private final int value;

        PageSize(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}