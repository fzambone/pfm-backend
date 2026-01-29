package dev.zambone.account.domain;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import dev.zambone.account.testing.AccountTestRig;
import dev.zambone.account.testing.CreditCardSettingsTestRig;
import dev.zambone.appuser.domain.UserContext;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.common.messages.ErrorMessages;
import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreditCardSettingsLogicTest {

  private AccountTestRig accountRig;
  private UUID householdId;
  private UserContext userContext;
  private Account testCreditCardAccount;
  private CreditCardSettingsLogic ccLogic;

  @BeforeEach
  void setup() {
    accountRig = new AccountTestRig();
    CreditCardSettingsTestRig ccRig = new CreditCardSettingsTestRig(accountRig.logic());
    ccLogic = ccRig.logic();

    householdId = UUID.randomUUID();
    userContext = new UserContext(UUID.randomUUID(), Set.of(householdId));

    testCreditCardAccount =
        accountRig.createPersistedAccount(
            "Test Credit Card",
            householdId,
            userContext.actorId(),
            AccountType.CREDIT_CARD);
  }

  @Test
  void shouldCreate_whenAccountIsCreditCard() {
    // Arrange
    var closingDay = 31;
    var dueDay = 1;
    var limitAmount = BigDecimal.valueOf(10000);

    // Act
    var creditCardSettings =
        ccLogic.create(
            userContext, testCreditCardAccount.id(), closingDay, dueDay, limitAmount);

    // Assert
    assertThat(creditCardSettings.accountId()).isEqualTo(testCreditCardAccount.id());
    assertThat(creditCardSettings.closingDay()).isEqualTo(31);
    assertThat(creditCardSettings.dueDay()).isEqualTo(1);
    assertThat(creditCardSettings.limitAmount()).isEqualTo(BigDecimal.valueOf(10000));
    assertThat(creditCardSettings.isActive()).isTrue();
    assertThat(creditCardSettings.createdAt()).isNotNull();
    assertThat(creditCardSettings.updatedAt()).isNotNull();
    assertThat(creditCardSettings.createdBy()).isEqualTo(userContext.actorId());
    assertThat(creditCardSettings.updatedBy()).isEqualTo(userContext.actorId());
  }

  @Test
  void shouldThrow_whenCcSettingsAlreadyExist() {
    // Arrange
    var closingDay = 31;
    var dueDay = 1;
    var limitAmount = BigDecimal.valueOf(10000);
    var testCreditCardAccountId = testCreditCardAccount.id();
    ccLogic.create(
        userContext, testCreditCardAccountId, closingDay, dueDay, limitAmount);

    // Act
    var exception = assertThrows(IllegalArgumentException.class, () -> ccLogic.create(
        userContext, testCreditCardAccountId, closingDay, dueDay, limitAmount));
    assertThat(exception.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_DUPLICATED_ENTRY);

  }

  @Test
  void shouldThrow_whenCreating_withInvalidDays() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var limitAmount = BigDecimal.valueOf(10000);

    var closingDayNormal = 10;
    var closingDayBelowMin = 0;
    var closingDayAboveMax = 32;

    var dueDayNormal = 15;
    var dueDayBelowMin = -1;
    var dueDayAboveMax = 35;

    // Act
    var exceptionDueDay = assertThrows(
        IllegalArgumentException.class,
        () ->
            ccLogic.create(
                userContext,
                testCreditCardAccountId,
                closingDayNormal,
                dueDayBelowMin,
                limitAmount));

    var exceptionDueDay2 = assertThrows(
        IllegalArgumentException.class,
        () ->
            ccLogic.create(
                userContext,
                testCreditCardAccountId,
                closingDayNormal,
                dueDayAboveMax,
                limitAmount));

    var exceptionClosingDay = assertThrows(
        IllegalArgumentException.class,
        () ->
            ccLogic.create(
                userContext,
                testCreditCardAccountId,
                closingDayBelowMin,
                dueDayNormal,
                limitAmount));

    var exceptionClosingDay2 = assertThrows(
        IllegalArgumentException.class,
        () ->
            ccLogic.create(
                userContext,
                testCreditCardAccountId,
                closingDayAboveMax,
                dueDayNormal,
                limitAmount));

    // Assert
    assertThat(exceptionDueDay.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_DUE_DAY);
    assertThat(exceptionDueDay2.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_DUE_DAY);
    assertThat(exceptionClosingDay.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_CLOSING_DAY);
    assertThat(exceptionClosingDay2.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_CLOSING_DAY);
  }

  @Test
  void shouldThrow_whenCreating_withNonCcAccount() {
    // Arrange
    var closingDay = 10;
    var dueDay = 15;
    var limitAmount = BigDecimal.valueOf(10000);

    var accountId =
        accountRig
            .createPersistedAccount(
                "Non CC Account",
                householdId,
                userContext.actorId(),
                AccountType.CHECKING)
            .id();

    // Act
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> ccLogic.create(userContext, accountId, closingDay, dueDay, limitAmount));

    // Assert
    assertThat(exception.getMessage()).isEqualTo(
        ErrorMessages.CREDIT_CARD_SETTINGS_INVALID_ACCOUNT_TYPE);

  }

  @Test
  void shouldGetByAccountId_whenCcSettingsExist() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var closingDay = 15;
    var dueDay = 20;
    var limitAmount = BigDecimal.valueOf(10000);

    var ccSettings = ccLogic.create(userContext, testCreditCardAccountId, closingDay, dueDay,
        limitAmount);

    // Act
    var foundCcSettings = ccLogic.get(userContext, ccSettings.accountId());

    // Assert
    assertThat(foundCcSettings).isNotNull();
    assertThat(foundCcSettings.updatedBy()).isEqualTo(userContext.actorId());
    assertThat(foundCcSettings.createdBy()).isEqualTo(userContext.actorId());
    assertThat(foundCcSettings.isActive()).isTrue();
    assertThat(foundCcSettings.limitAmount()).isEqualTo(limitAmount);
    assertThat(foundCcSettings.dueDay()).isEqualTo(dueDay);
    assertThat(foundCcSettings.closingDay()).isEqualTo(closingDay);
  }

  @Test
  void shouldThrow_whenCcSettingsNotExist() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var closingDay = 15;
    var dueDay = 20;
    var limitAmount = BigDecimal.valueOf(10000);
    var randomId = UUID.randomUUID();

    ccLogic.create(userContext, testCreditCardAccountId, closingDay, dueDay,
        limitAmount);

    // Act
    var exception = assertThrows(ResourceNotFoundException.class,
        () -> ccLogic.get(userContext, randomId));

    // Assert
    assertThat(exception.getMessage()).isEqualTo(
        ErrorMessages.creditCardSettingsNotFound(randomId));
  }

  @Test
  void shouldThrow_whenUserDoesNotHaveAccessToAccount() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var closingDay = 15;
    var dueDay = 20;
    var limitAmount = BigDecimal.valueOf(10000);
    var ccSettings = ccLogic.create(userContext, testCreditCardAccountId, closingDay, dueDay,
        limitAmount);
    var accountId = ccSettings.accountId();

    var differentHouseholdId = UUID.randomUUID();
    var unauthorizedUser = new UserContext(UUID.randomUUID(), Set.of(differentHouseholdId));

    // Act
    var exception = assertThrows(IllegalArgumentException.class,
        () -> ccLogic.get(unauthorizedUser, accountId));

    // Assert
    assertThat(exception.getMessage()).isEqualTo(
        ErrorMessages.accountNotFound(ccSettings.accountId()));
  }

  @Test
  void shouldUpdateSuccessfully() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var closingDay = 15;
    var dueDay = 20;
    var limitAmount = BigDecimal.valueOf(10000);
    var ccSettings = ccLogic.create(userContext, testCreditCardAccountId, closingDay, dueDay,
        limitAmount);
    var version = ccSettings.updatedAt();

    // Act
    var newClosingDay = 17;
    var newDueDay = 22;
    var newLimitAmount = BigDecimal.valueOf(15000);
    var updatedCcSettings = ccLogic.update(userContext, testCreditCardAccountId, newClosingDay,
        newDueDay, newLimitAmount, version);

    // Assert
    assertThat(updatedCcSettings.accountId()).isEqualTo(ccSettings.accountId());
    assertThat(updatedCcSettings.closingDay()).isEqualTo(newClosingDay);
    assertThat(updatedCcSettings.dueDay()).isEqualTo(newDueDay);
    assertThat(updatedCcSettings.limitAmount()).isEqualTo(newLimitAmount);
    assertThat(updatedCcSettings.updatedAt()).isNotEqualTo(ccSettings.updatedAt());
  }

  @Test
  void shouldThrow_whenUpdating_withStaleVersion() {
    // Arrange
    var testCreditCardAccountId = testCreditCardAccount.id();
    var closingDay = 15;
    var dueDay = 20;
    var limitAmount = BigDecimal.valueOf(10000);
    var ccSettings = ccLogic.create(userContext, testCreditCardAccountId, closingDay, dueDay,
        limitAmount);
    var version = ccSettings.updatedAt().minusSeconds(10);

    // Act
    var newClosingDay = 17;
    var newDueDay = 22;
    var newLimitAmount = BigDecimal.valueOf(15000);
    var exception = assertThrows(ConcurrentModificationException.class,
        () -> ccLogic.update(userContext, testCreditCardAccountId, newClosingDay,
            newDueDay, newLimitAmount, version));

    // Assert
    assertThat(exception.getMessage()).isEqualTo(ErrorMessages.CONCURRENT_DATA_MODIFIED);
  }
}
