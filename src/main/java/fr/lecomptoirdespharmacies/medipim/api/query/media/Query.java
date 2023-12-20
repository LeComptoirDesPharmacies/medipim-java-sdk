package fr.lecomptoirdespharmacies.medipim.api.query.media;

import fr.lecomptoirdespharmacies.medipim.api.query.QueryPage;

public record Query(QueryFilter filter,
                    QuerySorting sorting,
                    QueryPage page) {}