package dev.zambone.appuser.domain;

import java.time.Instant;
import java.util.UUID;

public record AppUser(
    UUID id,
    String email,
    String passwordHash,
    String fullName,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID createdBy,
    UUID updatedBy
) {
  public AppUser withDeletedAt(Instant newDeletedAt) {
    return new AppUser(id, email, passwordHash, fullName, createdAt, updatedAt, newDeletedAt, createdBy, updatedBy);
  }

  public AppUser withFullName (String newFullName) {
    return new AppUser(id, email, passwordHash, newFullName, createdAt, updatedAt, null, createdBy, updatedBy);
  }

  public AppUser withUpdatedBy (UUID newUpdatedBy) {
    return new AppUser(id, email, passwordHash, fullName, createdAt, updatedAt, null, createdBy, newUpdatedBy);
  }

  public AppUser withUpdatedAt (Instant newUpdatedAt) {
    return new AppUser(id, email, passwordHash, fullName, createdAt, newUpdatedAt, null, createdBy, updatedBy);
  }

  public AppUser withPasswordHash(String newPasswordHash) {
    return new AppUser(id, email, newPasswordHash, fullName, createdAt, updatedAt, null, createdBy, updatedBy);
  }
}
