package dev.zambone.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Account(
    UUID id,
    UUID householdId,
    String name,
    AccountType type,
    CurrencyCode currencyCode,
    BigDecimal currentBalance,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID createdBy,
    UUID updatedBy
) {

  public Account withName(String newName) {
    return new Account(id, householdId, newName, type, currencyCode, currentBalance, isActive, createdAt, updatedAt, deletedAt, createdBy, updatedBy);
  }

  public Account withUpdatedBy(UUID newUpdatedBy) {
    return new Account(id, householdId, name, type, currencyCode, currentBalance, isActive, createdAt, updatedAt, deletedAt, createdBy, newUpdatedBy);
  }

  public Account withUpdatedAt(Instant newUpdatedAt) {
    return new Account(id, householdId, name, type, currencyCode, currentBalance, isActive, createdAt, newUpdatedAt, deletedAt, createdBy, updatedBy);
  }

  public Account withDeletedAt(Instant newDeletedAt) {
    return new Account(id, householdId, name, type, currencyCode, currentBalance, isActive, createdAt, updatedAt, newDeletedAt, createdBy, updatedBy);
  }

}
