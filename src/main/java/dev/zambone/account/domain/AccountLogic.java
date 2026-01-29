package dev.zambone.account.domain;

import dev.zambone.appuser.domain.UserContext;
import dev.zambone.common.exceptions.DuplicateResourceNameException;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.common.messages.ErrorMessages;
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

    if (accountName.isBlank()) {
      throw new IllegalArgumentException("Account name cannot be blank or null");
    }

    if (!context.hasAccessToHousehold(householdId)) {
      throw new ResourceNotFoundException("account not found");
    }

    var account =
        new Account(
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
            context.actorId());

    if (repository.findByName(householdId, accountName).isPresent()) {
      throw new DuplicateResourceNameException("Account", "name", accountName);
    }

    return repository.save(account);
  }

  public Account get(UserContext context, UUID accountId) {
    var account =
        repository
            .findById(accountId, context.actorId())
            .orElseThrow(
                () -> new ResourceNotFoundException(ErrorMessages.accountNotFound(accountId)));

    validateAccount(account, context);

    return account;
  }

  public Account update(UUID accountId, String newName, UserContext context, Instant version) {
    var existingAccount =
        repository
            .findById(accountId, context.actorId())
            .orElseThrow(
                () -> new IllegalArgumentException(ErrorMessages.accountNotFound(accountId)));

    validateAccount(existingAccount, context);

    var updatedAccount =
        existingAccount
            .withName(newName)
            .withUpdatedBy(context.actorId())
            .withUpdatedAt(Instant.now());

    if (!repository.update(updatedAccount, version)) {
      throw new ConcurrentModificationException(ErrorMessages.CONCURRENT_DATA_MODIFIED);
    }

    return updatedAccount;
  }

  public Account delete(UUID accountId, UserContext context, Instant version) {
    var existingAccount =
        repository
            .findById(accountId, context.actorId())
            .orElseThrow(
                () -> new IllegalArgumentException(ErrorMessages.accountNotFound(accountId)));

    validateAccount(existingAccount, context);

    var deletedAccount =
        existingAccount
            .withDeletedAt(Instant.now())
            .withUpdatedAt(Instant.now())
            .withUpdatedBy(context.actorId());

    boolean success = repository.delete(deletedAccount, version);

    if (!success) {
      throw new ConcurrentModificationException("Data was modified by another user");
    }

    return deletedAccount;
  }

  private void validateAccount(Account account, UserContext context) {
    if (account.deletedAt() != null) {
      throw new ResourceNotFoundException(ErrorMessages.accountNotFound(account.id()));
    }

    if (!context.hasAccessToHousehold(account.householdId())) {
      throw new IllegalArgumentException(ErrorMessages.accountNotFound(account.id()));
    }
  }
}
