package fr.lcdp.medipim.api.query.products;

import fr.lcdp.medipim.api.query.QueryPage;

public record Query(QueryFilter filter,
                    QuerySorting sorting,
                    QueryPage page) {}