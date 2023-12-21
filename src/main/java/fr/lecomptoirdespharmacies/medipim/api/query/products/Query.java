package fr.lecomptoirdespharmacies.medipim.api.query.products;

import fr.lecomptoirdespharmacies.medipim.api.query.QueryPage;

public record Query(QueryFilter filter,
                    QuerySorting sorting,
                    QueryPage page) {}