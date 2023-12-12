package fr.lcdp.medipim.api.query;

import java.time.Instant;

public record TimelineAnswer<T>(PaginatedResponse<T> response,
                                Instant validUntil) {
}
