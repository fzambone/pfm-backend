package dev.zambone.household.domain;

import java.time.Instant;
import java.util.UUID;

public record HouseholdMember(
    UUID householdId,
    UUID appUserId,
    Role role,
    Instant joinedAt,
    Instant deletedAt,
    UUID invitedBy
) {
}
