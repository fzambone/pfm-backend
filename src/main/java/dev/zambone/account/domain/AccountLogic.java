package dev.zambone.account.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;
import dev.zambone.appuser.domain.UserContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class AccountLogic {

  private final AccountRepository repository;

  public AccountLogic(AccountRepository repository) {
    this.repository = repository;
  }

  public Account create(
      UUID householdId,
      String accountName,
      AccountType accountType,
      CurrencyCode currencyCode,
      UserContext context) {

    if (!context.hasAccessToHousehold(householdId)) {
      throw new ResourceNotFoundException("Account not found");
    }

    var account = new Account(
        UUID.randomUUID(),
        householdId,
        accountName,
        accountType,
        currencyCode,
        BigDecimal.ZERO,
        true,
        Instant.now(),
        Instant.now(),
        null,
        context.actorId(),
        context.actorId()
        );

    return repository.save(account);
  }

  public Account get(UserContext context, UUID id) {
    var account = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    validateAccount(account, context);

    return account;
  }

  public Account update(UUID id, String newName, UserContext context, Instant version) {
    var existingAccount = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));

    validateAccount(existingAccount, context);

    var updatedAccount = existingAccount
        .withName(newName)
        .withUpdatedBy(context.actorId())
        .withUpdatedAt(Instant.now());

    boolean success = repository.update(updatedAccount, version);
    if (!success) {
      throw new ConcurrentModificationException("Data was modified by another user");
    }

    return updatedAccount;
  }

  public Account delete(UUID id, UserContext context, Instant version) {
    var existingAccount = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

    validateAccount(existingAccount, context);

    var deletedAccount = existingAccount
        .withDeletedAt(Instant.now())
        .withUpdatedAt(Instant.now())
        .withUpdatedBy(context.actorId());

    boolean success = repository.delete(deletedAccount, version);

    if (!success) throw new ConcurrentModificationException("Data was modified by another user");

    return deletedAccount;
  }

  private void validateAccount(Account account, UserContext context) {
    if (account.deletedAt() != null) {
      throw new ResourceNotFoundException("Account not found");
    }

    if (!context.hasAccessToHousehold(account.householdId())) {
      throw new IllegalArgumentException("Account not found");
    }
  }
}
