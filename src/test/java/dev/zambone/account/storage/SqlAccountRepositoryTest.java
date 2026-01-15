package dev.zambone.account.storage;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CurrencyCode;
import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.domain.AppUserRepository;
import dev.zambone.appuser.storage.SqlAppUserRepository;
import dev.zambone.appuser.testing.AppUserFactory;
import dev.zambone.household.domain.HouseholdMemberRepository;
import dev.zambone.household.domain.HouseholdRepository;
import dev.zambone.household.storage.SqlHouseholdMemberRepository;
import dev.zambone.household.storage.SqlHouseholdRepository;
import dev.zambone.household.testing.HouseholdFactory;
import dev.zambone.testing.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

public class SqlAccountRepositoryTest {

  private HouseholdFactory householdFactory;
  private AppUserFactory appUserFactory;
  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();
    AppUserRepository appUserRepository = new SqlAppUserRepository(dataSource);
    HouseholdRepository householdRepository = new SqlHouseholdRepository(dataSource);
    HouseholdMemberRepository householdMemberRepository = new SqlHouseholdMemberRepository(dataSource);
    accountRepository = new SqlAccountRepository(dataSource);
    householdFactory = new HouseholdFactory(householdRepository, householdMemberRepository);
    appUserFactory = new AppUserFactory(appUserRepository);
  }

  @Test
  void shouldSaveAndFindAccount() {
    var user =  appUserFactory.createAndPersistUser("test_account@zambone.dev");
    var household = householdFactory.createAndPersistHousehold(
        "Test Household",
        user
    );

    var account = new Account(
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
        user.id()
    );

    var savedAccount = accountRepository.save(account);
    assertThat(savedAccount).isNotNull();
    assertThat(savedAccount.name()).isEqualTo("Test Account");
    assertThat(savedAccount).isEqualTo(account);
  }
}
