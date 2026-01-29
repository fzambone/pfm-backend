package dev.zambone.account.storage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CreditCardSettings;
import dev.zambone.account.domain.CreditCardSettingsRepository;
import dev.zambone.account.testing.AccountFactory;
import dev.zambone.account.testing.CreditCardSettingsFactory;
import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.domain.AppUserRepository;
import dev.zambone.appuser.storage.SqlAppUserRepository;
import dev.zambone.appuser.testing.AppUserFactory;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.common.messages.ErrorMessages;
import dev.zambone.household.domain.Household;
import dev.zambone.household.domain.HouseholdMemberRepository;
import dev.zambone.household.domain.HouseholdRepository;
import dev.zambone.household.storage.SqlHouseholdMemberRepository;
import dev.zambone.household.storage.SqlHouseholdRepository;
import dev.zambone.household.testing.HouseholdFactory;
import dev.zambone.testing.TestDatabase;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlCreditCardSettingsTest {

  private AppUserFactory appUserFactory;
  private HouseholdFactory householdFactory;
  private AccountFactory accountFactory;
  private CreditCardSettingsFactory creditCardSettingsFactory;
  private CreditCardSettingsRepository creditCardSettingsRepository;
  private AppUser user;
  private Household household;
  private Account account;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();
    TestDatabase.reset();

    AppUserRepository appUserRepository = new SqlAppUserRepository(dataSource);
    HouseholdRepository householdRepository = new SqlHouseholdRepository(dataSource);
    HouseholdMemberRepository householdMemberRepository = new SqlHouseholdMemberRepository(
        dataSource);
    AccountRepository accountRepository = new SqlAccountRepository(dataSource);

    appUserFactory = new AppUserFactory(appUserRepository);
    householdFactory = new HouseholdFactory(householdRepository, householdMemberRepository);
    accountFactory = new AccountFactory(accountRepository);
    user = appUserFactory.createAndPersistUser("test_ccsettings@zambone.dev");
    household = householdFactory.createAndPersistHousehold("CC Test Household", user);
    account = accountFactory.createAndPersistAccount("CC Test Account", household.id(), user.id(),
        AccountType.CREDIT_CARD);

    creditCardSettingsRepository = new SqlCreditCardSettingsRepository(dataSource);
    creditCardSettingsFactory = new CreditCardSettingsFactory(creditCardSettingsRepository);
  }

  @Test
  void shouldSaveAccount_successfully() {
    // Arrange
    var ccSettings = new CreditCardSettings(
        UUID.randomUUID(),
        account.id(),
        10,
        15,
        BigDecimal.valueOf(10000),
        true,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        user.id(),
        user.id());

    // Act
    var savedCcSettings = creditCardSettingsRepository.save(ccSettings);

    // Assert
    assertThat(savedCcSettings.id()).isNotNull();
    assertThat(savedCcSettings.accountId()).isEqualTo(account.id());
    assertThat(savedCcSettings.closingDay()).isEqualTo(10);
    assertThat(savedCcSettings.dueDay()).isEqualTo(15);
    assertThat(savedCcSettings.limitAmount().compareTo(BigDecimal.valueOf(10000))).isEqualTo(0);
    assertThat(savedCcSettings.isActive()).isTrue();
    assertThat(savedCcSettings.createdAt()).isNotNull();
    assertThat(savedCcSettings.updatedAt()).isNotNull();
    assertThat(savedCcSettings.deletedAt()).isNull();
    assertThat(savedCcSettings.createdBy()).isEqualTo(user.id());
    assertThat(savedCcSettings.updatedBy()).isEqualTo(user.id());
  }

  @Test
  void shouldFindExistingCcSettingsByAccountId_successfully() {
    // Arrange
    var ccSettings = creditCardSettingsFactory.createAndPersistCreditCardSettings(account.id(), 10,
        15, BigDecimal.valueOf(10000), user.id());

    // Act
    var fetchedCcSettings = creditCardSettingsRepository.findByAccountId(account.id());

    // Assert
    assertThat(fetchedCcSettings.isPresent()).isTrue();
    assertThat(fetchedCcSettings.get().id()).isEqualTo(ccSettings.id());
    assertThat(fetchedCcSettings.get().accountId()).isEqualTo(ccSettings.accountId());
    assertThat(fetchedCcSettings.get().isActive()).isTrue();
  }

  @Test
  void shouldThrow_whenFindingCcSettings_NotExist() {
    // Arrange
    creditCardSettingsFactory.createAndPersistCreditCardSettings(account.id(), 10,
        15, BigDecimal.valueOf(10000), user.id());
    var randomId = UUID.randomUUID();

    // Act
    var exception = assertThrows(ResourceNotFoundException.class,
        () -> creditCardSettingsRepository.findByAccountId(randomId));

    // Assert
    assertThat(exception.getMessage()).isEqualTo(
        ErrorMessages.creditCardSettingsNotFound(randomId));
  }

  @Test
  void shouldUpdateExistingCcSettings_successfully() {
    // Arrange
    var ccSettings = creditCardSettingsFactory.createAndPersistCreditCardSettings(account.id(), 10,
        15, BigDecimal.valueOf(10000), user.id());
    var version = ccSettings.updatedAt();

    // Act
    var updatedCcSettings = ccSettings.withClosingDay(20)
        .withDueDay(25)
        .withLimitAmount(BigDecimal.valueOf(30000))
        .withUpdatedAt(Instant.now())
        .withUpdatedBy(user.id());
    var success = creditCardSettingsRepository.update(updatedCcSettings, version);
    var fetchedCcSettings = creditCardSettingsRepository.findByAccountId(ccSettings.accountId());

    // Assert
    assertThat(success).isTrue();
    assertThat(fetchedCcSettings.isPresent()).isTrue();
    assertThat(fetchedCcSettings.get().closingDay()).isEqualTo(updatedCcSettings.closingDay());
    assertThat(fetchedCcSettings.get().dueDay()).isEqualTo(updatedCcSettings.dueDay());
    assertThat(
        fetchedCcSettings.get().limitAmount().compareTo(updatedCcSettings.limitAmount())).isEqualTo(
        0);
    assertThat(fetchedCcSettings.get().updatedAt()).isEqualTo(updatedCcSettings.updatedAt());
    assertThat(fetchedCcSettings.get().updatedBy()).isEqualTo(updatedCcSettings.updatedBy());
  }

  @Test
  void shouldFail_whenUpdatingCcSettings_withStaleVersion() {
    // Arrange
    var ccSettings = creditCardSettingsFactory.createAndPersistCreditCardSettings(account.id(), 10,
        15, BigDecimal.valueOf(10000), user.id());
    var version = ccSettings.updatedAt().minusSeconds(10);

    // Act
    var updatedCcSettings = ccSettings.withClosingDay(20)
        .withDueDay(25)
        .withLimitAmount(BigDecimal.valueOf(30000))
        .withUpdatedAt(Instant.now())
        .withUpdatedBy(user.id());
    var success = creditCardSettingsRepository.update(updatedCcSettings, version);
    var fetchedCcSettings = creditCardSettingsRepository.findByAccountId(ccSettings.accountId());

    // Assert
    assertThat(success).isFalse();
    assertThat(fetchedCcSettings.isPresent()).isTrue();
    assertThat(fetchedCcSettings.get()).isEqualTo(ccSettings);
    assertThat(fetchedCcSettings.get()).isNotEqualTo(updatedCcSettings);
  }

}
