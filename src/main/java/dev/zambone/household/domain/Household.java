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

  public Household withName(String newName) {
    return new Household(id, newName, isActive, createdAt, updatedAt, createdBy, updatedBy);
  }

  public Household withUpdatedAt(Instant newUpdatedAt) {
    return new Household(id, name, isActive, createdAt, newUpdatedAt, createdBy, updatedBy);
  }

  public Household withUpdatedBy(UUID newUpdatedBy) {
    return new Household(id, name, isActive, createdAt, updatedAt, createdBy, newUpdatedBy);
  }
}
