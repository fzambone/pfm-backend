package dev.zambone.appusers.domain;

import java.time.Instant;
import java.util.UUID;

public record AppUser(
    UUID id,
    String email,
    String passwordHash,
    String fullName,
    Instant createdAt,
    Instant createdBy
) {
}
