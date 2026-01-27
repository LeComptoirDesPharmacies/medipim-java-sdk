package fr.lecomptoirdespharmacies.medipim.api.query;

import java.util.Set;

public record QueryPage(int no, int size) {
    private static final Set<Integer> VALID_SIZES = Set.of(10, 50, 100, 250);

    public QueryPage {
        if (!VALID_SIZES.contains(size)) {
            throw new IllegalArgumentException("size must be one of: " + VALID_SIZES);
        }
    }
}