package fr.lecomptoirdespharmacies.medipim.api.query;


import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

public record PaginatedResponse<T>(boolean isExhaustive,
                                   List<T> results) {

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(results);
    }
}
