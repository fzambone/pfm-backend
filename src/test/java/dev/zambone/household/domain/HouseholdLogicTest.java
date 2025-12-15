package dev.zambone.household.domain;

import dev.zambone.appusers.domain.UserContext;
import dev.zambone.household.testing.FakeHouseholdMemberRepository;
import dev.zambone.household.testing.FakeHouseholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class HouseholdLogicTest {


  private HouseholdLogic householdLogic;
  private FakeHouseholdMemberRepository fakeHouseholdMemberRepository;

  @BeforeEach
  void setUp() {
    FakeHouseholdRepository fakeHouseholdRepository = new FakeHouseholdRepository();
    fakeHouseholdMemberRepository = new FakeHouseholdMemberRepository();

    householdLogic = new HouseholdLogic(fakeHouseholdRepository, fakeHouseholdMemberRepository);
  }

  @Test
  public void shouldCreate_successfullyWithAdmin() {
    var userId = UUID.randomUUID();
    var createdHousehold = householdLogic.create(
        new UserContext(userId),
        "Test household"
    );

    assertThat(createdHousehold).isNotNull();
    assertThat(createdHousehold.id()).isNotNull();

    var adminMember = fakeHouseholdMemberRepository.findByMemberId(userId);

    assertThat(createdHousehold.name()).isEqualTo("Test household");
    assertThat(adminMember.isPresent()).isTrue();
    assertThat(adminMember.get().appUserId()).isEqualTo(userId);
    assertThat(adminMember.get().role()).isEqualTo(Role.ADMIN);
  }

  @Test
  public void shouldGetHousehold_successfully_whenUserIsMember() {
    var userId = UUID.randomUUID();
    var context = new UserContext(userId);

    var createdHousehold = householdLogic.create(
        context,
        "Test household"
    );

    var fetchedHousehold = householdLogic.getHousehold(context, createdHousehold.id());

    assertThat(fetchedHousehold).isNotNull();
    assertThat(fetchedHousehold.name()).isEqualTo("Test household");
    assertThat(fetchedHousehold.createdBy()).isEqualTo(userId);
    assertThat(fetchedHousehold.isActive()).isTrue();
  }

  @Test
  public void shouldThrowException_whenGettingHousehold_ifUserIsNotMember() {
    var ownerId = UUID.randomUUID();
    var intruderId = UUID.randomUUID();

    var createdHousehold = householdLogic.create(
        new UserContext(ownerId),
        "Secret household"
    );

    assertThrows(IllegalArgumentException.class, () -> {
      householdLogic.getHousehold(new UserContext(intruderId), createdHousehold.id());
    });
  }
}
