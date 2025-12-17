package dev.zambone.household.storage;

import dev.zambone.appusers.storage.SqlAppUserRepository;
import dev.zambone.appusers.testing.AppUserFactory;
import dev.zambone.household.domain.HouseholdMember;
import dev.zambone.household.domain.Role;
import dev.zambone.household.testing.HouseholdFactory;
import dev.zambone.testing.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.google.common.truth.Truth.assertThat;

public class SqlHouseholdMemberRepositoryTest {

  private SqlHouseholdRepository householdRepository;
  private SqlAppUserRepository appUserRepository;
  private SqlHouseholdMemberRepository householdMemberRepository;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();

    TestDatabase.reset();

    householdRepository = new SqlHouseholdRepository(dataSource);
    appUserRepository = new SqlAppUserRepository(dataSource);
    householdMemberRepository = new SqlHouseholdMemberRepository(dataSource);
  }

  @Test
  void shouldSaveAndFindHouseholdMember() {
    var adminUser = AppUserFactory.createAndPersistUser(appUserRepository, "test@zambone.dev");
    var memberUser = AppUserFactory.createAndPersistUser(appUserRepository, "test_member@zambone.dev");
    var household = HouseholdFactory.createAndSaveHousehold(householdRepository, householdMemberRepository, "Test Household", adminUser);
    var householdMember = new HouseholdMember(
        household.id(),
        memberUser.id(),
        Role.MEMBER,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        adminUser.id()
    );

    householdMemberRepository.save(householdMember);

    var foundMember = householdMemberRepository.findByMemberId(householdMember.appUserId());
    assertThat(foundMember.isPresent()).isTrue();
    assertThat(foundMember.get()).isEqualTo(householdMember);
  }
}
