package dev.zambone.account.domain;

import static java.time.ZoneOffset.UTC;

import dev.zambone.appuser.domain.UserContext;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.common.messages.ErrorMessages;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class CreditCardSettingsLogic {

  private final CreditCardSettingsRepository repository;
  private final AccountLogic accountLogic;

  public CreditCardSettingsLogic(
      CreditCardSettingsRepository repository, AccountLogic accountLogic) {
    this.repository = repository;
    this.accountLogic = accountLogic;
  }

  public CreditCardSettings create(
      UserContext userContext,
      UUID accountId,
      int closingDay,
      int dueDay,
      BigDecimal limitAmount) {

    validateSettings(userContext, accountId, closingDay, dueDay);

    if (repository.findByAccountId(accountId).isPresent()) {
      throw new IllegalArgumentException(
          ErrorMessages.CREDIT_CARD_SETTINGS_DUPLICATED_ENTRY);
    }

    var creditCardSettings =
        new CreditCardSettings(
            UUID.randomUUID(),
            accountId,
            closingDay,
            dueDay,
            limitAmount,
            true,
            Instant.now(),
            Instant.now(),
            null,
            userContext.actorId(),
            userContext.actorId());

    return repository.save(creditCardSettings);
  }

  public CreditCardSettings get(UserContext userContext, UUID accountId) {
    var ccSettings = repository.findByAccountId(accountId);
    if (ccSettings.isEmpty()) {
      throw new ResourceNotFoundException(
          ErrorMessages.creditCardSettingsNotFound(accountId));
    }

    // Verify account access
    accountLogic.get(userContext, ccSettings.get().accountId());

    return ccSettings.get();
  }

  public CreditCardSettings update(UserContext userContext, UUID accountId, int closingDay,
      int dueDay, BigDecimal limitAmount, Instant version) {

    var existingCcSettings = get(userContext, accountId);
    validateSettings(userContext, accountId, closingDay, dueDay);

    var updatedCcSettings = existingCcSettings
        .withClosingDay(closingDay)
        .withDueDay(dueDay)
        .withLimitAmount(limitAmount)
        .withUpdatedAt(Instant.now())
        .withUpdatedBy(userContext.actorId());

    if (!repository.update(updatedCcSettings, version)) {
      throw new ConcurrentModificationException(ErrorMessages.CONCURRENT_DATA_MODIFIED);
    }

    return updatedCcSettings;
  }

  private void validateSettings(UserContext userContext, UUID accountId, int closingDay,
      int dueDay) {
    var account = accountLogic.get(userContext, accountId);

    if (account.type() != AccountType.CREDIT_CARD) {
      throw new IllegalArgumentException(
          ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_ACCOUNT_TYPE);
    }

    if (closingDay < 1 || closingDay > 31) {
      throw new IllegalArgumentException(
          ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_CLOSING_DAY);
    }
    if (dueDay < 1 || dueDay > 31) {
      throw new IllegalArgumentException(ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_DUE_DAY);
    }
  }
}
