package dev.zambone.household.domain;

import java.time.Instant;
import java.util.UUID;

public record Household(
    UUID id,
    String name,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    UUID createdBy,
    UUID updatedBy
) {
}
