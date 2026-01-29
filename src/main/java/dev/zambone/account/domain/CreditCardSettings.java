package dev.zambone.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditCardSettings(
    UUID id,
    UUID accountId,
    int closingDay,
    int dueDay,
    BigDecimal limitAmount,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID createdBy,
    UUID updatedBy) {

  public CreditCardSettings withClosingDay(int newClosingDay) {
    return new CreditCardSettings(id, accountId, newClosingDay, dueDay, limitAmount, isActive,
        createdAt, updatedAt, deletedAt, createdBy, updatedBy);
  }

  public CreditCardSettings withDueDay(int newDueDay) {
    return new CreditCardSettings(id, accountId, closingDay, newDueDay, limitAmount, isActive,
        createdAt, updatedAt, deletedAt, createdBy, updatedBy);
  }

  public CreditCardSettings withLimitAmount(BigDecimal newLimitAmount) {
    return new CreditCardSettings(id, accountId, closingDay, dueDay, newLimitAmount, isActive,
        createdAt, updatedAt, deletedAt, createdBy, updatedBy);
  }

  public CreditCardSettings withUpdatedAt(Instant newUpdatedAt) {
    return new CreditCardSettings(id, accountId, closingDay, dueDay, limitAmount, isActive,
        createdAt, newUpdatedAt, deletedAt, createdBy, updatedBy);
  }

  public CreditCardSettings withUpdatedBy(UUID newUpdatedBy) {
    return new CreditCardSettings(id, accountId, closingDay, dueDay, limitAmount, isActive,
        createdAt, updatedAt, deletedAt, createdBy, newUpdatedBy);
  }
}
