package dev.zambone.household.domain;

import java.time.Instant;
import java.util.UUID;

public record Household(
    UUID id,
    String name,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID createdBy,
    UUID updatedBy
) {

  public Household withName(String newName) {
    return new Household(id, newName, isActive, createdAt, updatedAt, this.deletedAt, createdBy, updatedBy);
  }

  public Household withUpdatedAt(Instant newUpdatedAt) {
    return new Household(id, name, isActive, createdAt, newUpdatedAt, this.deletedAt, createdBy, updatedBy);
  }

  public Household withDeletedAt(Instant newDeletedAt) {
    return new Household(id, name, isActive, createdAt, updatedAt, newDeletedAt, createdBy, updatedBy);
  }

  public Household withUpdatedBy(UUID newUpdatedBy) {
    return new Household(id, name, isActive, createdAt, updatedAt, this.deletedAt, createdBy, newUpdatedBy);
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
