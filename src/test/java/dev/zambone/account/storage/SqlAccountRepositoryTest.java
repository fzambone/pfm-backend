package dev.zambone.account.storage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CurrencyCode;
import dev.zambone.account.testing.AccountFactory;
import dev.zambone.appuser.domain.AppUserRepository;
import dev.zambone.appuser.storage.SqlAppUserRepository;
import dev.zambone.appuser.testing.AppUserFactory;
import dev.zambone.common.exceptions.ResourceNotFoundException;
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

class SqlAccountRepositoryTest {

    private HouseholdFactory householdFactory;
    private AppUserFactory appUserFactory;
    private AccountFactory accountFactory;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = TestDatabase.getDataSource();
        TestDatabase.reset();

        AppUserRepository appUserRepository = new SqlAppUserRepository(dataSource);
        HouseholdRepository householdRepository = new SqlHouseholdRepository(dataSource);
        HouseholdMemberRepository householdMemberRepository =
                new SqlHouseholdMemberRepository(dataSource);

        accountRepository = new SqlAccountRepository(dataSource);

        householdFactory = new HouseholdFactory(householdRepository, householdMemberRepository);
        appUserFactory = new AppUserFactory(appUserRepository);
        accountFactory = new AccountFactory(accountRepository);
    }

    @Test
    void shouldSaveAccount_successfully() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_account@zambone.dev");
        var household = householdFactory.createAndPersistHousehold("Test Household", user);
        var account =
                new Account(
                        UUID.randomUUID(),
                        household.id(),
                        "Test Account",
                        AccountType.CHECKING,
                        CurrencyCode.USD,
                        BigDecimal.ZERO,
                        true,
                        Instant.now().truncatedTo(ChronoUnit.MICROS),
                        Instant.now().truncatedTo(ChronoUnit.MICROS),
                        null,
                        user.id(),
                        user.id());

        // Act
        var savedAccount = accountRepository.save(account);

        // Assert
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.name()).isEqualTo("Test Account");
        assertThat(savedAccount).isEqualTo(account);
    }

    @Test
    void shouldThrow_whenCreatingAccount_withEmptyName() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_account@zambone.dev");
        var household = householdFactory.createAndPersistHousehold("Test Household", user);
        var account =
                new Account(
                        UUID.randomUUID(),
                        household.id(),
                        "",
                        AccountType.CHECKING,
                        CurrencyCode.USD,
                        BigDecimal.ZERO,
                        true,
                        Instant.now().truncatedTo(ChronoUnit.MICROS),
                        Instant.now().truncatedTo(ChronoUnit.MICROS),
                        null,
                        user.id(),
                        user.id());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountRepository.save(account));
    }

    @Test
    void shouldFindExistingAccountById_successfully() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_account@zambone.dev");
        var household = householdFactory.createAndPersistHousehold("Account Household", user);
        var account =
                accountFactory.createAndPersistAccount("Test Account", household.id(), user.id());

        // Act
        var fetchedAccount = accountRepository.findById(account.id(), user.id());

        // Assert
        assertThat(fetchedAccount.isPresent()).isTrue();
        assertThat(fetchedAccount.get().id()).isEqualTo(account.id());
        assertThat(fetchedAccount.get().name()).isEqualTo(account.name());
    }

    @Test
    void shouldThrow_whenFindExistingAccountById_withoutAccess() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_account@zambone.dev");
        var randomUser = UUID.randomUUID();
        var household = householdFactory.createAndPersistHousehold("Account Household", user);
        var account =
                accountFactory.createAndPersistAccount("Test Account", household.id(), user.id());
        var accountId = account.id();

        // Act && Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> accountRepository.findById(accountId, randomUser));
    }

    @Test
    void shouldThrow_whenFindingNonExistingAccountById() {
        var accountId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        assertThrows(
                ResourceNotFoundException.class,
                () -> accountRepository.findById(accountId, userId));
    }

    @Test
    void shouldUpdate_successfully() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_update@zambone.dev");
        var userId = user.id();
        var household = householdFactory.createAndPersistHousehold("Test Update Account", user);
        var householdId = household.id();
        var account =
                accountFactory.createAndPersistAccount("Test Update Account", householdId, userId);
        var originalVersion = account.updatedAt();

        // Act
        var changedAccount =
                account.withName("Updated Test Name")
                        .withBalance(BigDecimal.TEN)
                        .withIsActive(false)
                        .withUpdatedBy(userId)
                        .withUpdatedAt(Instant.now());
        var success = accountRepository.update(changedAccount, originalVersion);
        var updatedAccount = accountRepository.findById(changedAccount.id(), userId);

        // Assert
        assertThat(success).isTrue();
        assertThat(updatedAccount.isPresent()).isTrue();
        assertThat(updatedAccount.get()).isNotEqualTo(account);
        assertThat(updatedAccount.get()).isEqualTo(changedAccount);
    }

    @Test
    void shouldFail_whenUpdating_withStaleVersion() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_update@zambone.dev");
        var userId = user.id();
        var household = householdFactory.createAndPersistHousehold("Test Update Account", user);
        var householdId = household.id();
        var account =
                accountFactory.createAndPersistAccount("Test Update Account", householdId, userId);

        // Act
        var changedAccount =
                account.withName("Updated Stale Test Name")
                        .withBalance(BigDecimal.TEN)
                        .withIsActive(false)
                        .withUpdatedBy(userId)
                        .withUpdatedAt(Instant.now());
        var success = accountRepository.update(changedAccount, Instant.now().minusSeconds(1000));
        var updatedAccount = accountRepository.findById(changedAccount.id(), userId);

        // Assert
        assertThat(success).isFalse();
        assertThat(updatedAccount.isPresent()).isTrue();
        assertThat(updatedAccount.get()).isEqualTo(account);
        assertThat(updatedAccount.get()).isNotEqualTo(changedAccount);
    }

    @Test
    void shouldDelete_successfully() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_delete@zambone.dev");
        var userId = user.id();
        var household = householdFactory.createAndPersistHousehold("Test Delete Account", user);
        var householdId = household.id();
        var account =
                accountFactory.createAndPersistAccount("Test Delete Account", householdId, userId);
        var originalVersion = account.updatedAt();
        var deletedAccount = account.withDeletedAt(Instant.now());
        var deletedAccountId = deletedAccount.id();

        // Act
        var success = accountRepository.delete(deletedAccount, originalVersion);

        // Assert
        assertThat(success).isTrue();
        assertThrows(
                ResourceNotFoundException.class,
                () -> accountRepository.findById(deletedAccountId, userId));
    }

    @Test
    void shouldFailDeleting_withStaleVersion() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_delete@zambone.dev");
        var userId = user.id();
        var household = householdFactory.createAndPersistHousehold("Test Delete Account", user);
        var householdId = household.id();
        var account =
                accountFactory.createAndPersistAccount("Test Delete Account", householdId, userId);
        var deletedAccount = account.withDeletedAt(Instant.now());
        var deletedAccountId = deletedAccount.id();

        // Act
        var success = accountRepository.delete(deletedAccount, Instant.now().minusSeconds(1000));
        var fetchedAccount = accountRepository.findById(deletedAccountId, userId);

        // Assert
        assertThat(success).isFalse();
        assertThat(fetchedAccount.isPresent()).isTrue();
        assertThat(fetchedAccount.get().deletedAt()).isNull();
    }

    @Test
    void shouldFindExistingAccountByName_successfully() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_find_by_name@zambone.dev");
        var userId = user.id();
        var household =
                householdFactory.createAndPersistHousehold("Test Find by Name Account", user);
        var householdId = household.id();
        var account =
                accountFactory.createAndPersistAccount(
                        "Test Find by Name Account", householdId, userId);

        // Act
        var foundAccount = accountRepository.findByName(householdId, "test find by name account");

        // Assert
        assertThat(foundAccount.isPresent()).isTrue();
        assertThat(foundAccount.get()).isEqualTo(account);
    }

    @Test
    void shouldReturnEmpty_whenAccountNotFound() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test@zambone.dev");
        var household = householdFactory.createAndPersistHousehold("Test Household", user);

        // Act
        var foundAccount = accountRepository.findByName(household.id(), "Nonexistent Account");

        // Assert
        assertThat(foundAccount.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnEmpty_whenAccountInDifferentHouseholdId() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_find_by_name@zambone.dev");
        var household1 =
                householdFactory.createAndPersistHousehold("Test Find by Name Account", user);
        var household2 =
                householdFactory.createAndPersistHousehold("Test Find by Name Account 2", user);
        accountFactory.createAndPersistAccount(
                "Test Find by Name Account", household1.id(), user.id());

        // Act
        var foundAccount = accountRepository.findByName(household2.id(), "Checking Name");

        // Assert
        assertThat(foundAccount.isEmpty()).isTrue();
    }

    @Test
    void shouldNotFind_whenNameOnlyPartiallyMatches() {
        // Arrange
        var user = appUserFactory.createAndPersistUser("test_find_by_name@zambone.dev");
        var household =
                householdFactory.createAndPersistHousehold("Test Find by Name Account", user);
        accountFactory.createAndPersistAccount("Test Account", household.id(), user.id());

        // Act
        var foundAccount = accountRepository.findByName(household.id(), "Test");

        // Assert
        assertThat(foundAccount.isEmpty()).isTrue();
    }
}
