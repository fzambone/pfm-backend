package dev.zambone.household.storage;

import dev.zambone.appuser.storage.SqlAppUserRepository;
import dev.zambone.appuser.testing.AppUserFactory;
import dev.zambone.household.domain.Role;
import dev.zambone.household.testing.HouseholdFactory;
import dev.zambone.testing.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class SqlHouseholdRepositoryTest {

  private SqlHouseholdRepository householdRepository;
  private SqlHouseholdMemberRepository householdMemberRepository;
  private AppUserFactory appUserFactory;
  private HouseholdFactory householdFactory;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();
    TestDatabase.reset();
    SqlAppUserRepository appUserRepository = new SqlAppUserRepository(dataSource);
    householdRepository = new SqlHouseholdRepository(dataSource);
    householdMemberRepository = new SqlHouseholdMemberRepository(dataSource);
    appUserFactory = new AppUserFactory(appUserRepository);
    householdFactory = new HouseholdFactory(householdRepository, householdMemberRepository);
  }

  @Test
  void shouldSaveAndFindHousehold_andVerifyAdminRole() {
    var user = appUserFactory.createAndPersistUser("save@zambone.dev");
    var household = householdFactory.createAndPersistHousehold("Test Household", user);

    var foundHousehold = householdRepository.findByIdAndUserId(household.id(), user.id());
    assertThat(foundHousehold.isPresent()).isTrue();
    assertThat(foundHousehold.get()).isEqualTo(household);

    // Verify ADMIN
    var membership = householdMemberRepository.findMembership(household.id(), user.id());
    assertThat(membership.isPresent()).isTrue();
    assertThat(membership.get().role()).isEqualTo(Role.ADMIN);
  }

  @Test
  void shouldThrowIllegalArgumentException_whenUserIsNotAMember() {
    var user = appUserFactory.createAndPersistUser("illegal@zambone.dev");
    var household = householdFactory.createAndPersistHousehold("Illegal household", user);

    assertThrows(IllegalArgumentException.class, () -> {
      householdRepository.findByIdAndUserId(household.id(), UUID.randomUUID());
    });
  }

  @Test
  void shouldUpdateHousehold_successfully_whenVersionMatches() {
    var user = appUserFactory.createAndPersistUser("update@zambone.dev");
    var household = householdFactory.createAndPersistHousehold("Update household", user);
    var updatedHousehold = householdRepository.update(household, household.updatedAt());
    assertThat(updatedHousehold).isTrue();

  }
}
