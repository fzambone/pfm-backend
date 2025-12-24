package dev.zambone.household.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;
import dev.zambone.appuser.domain.UserContext;
import dev.zambone.appuser.testing.AppUserTestRig;
import dev.zambone.household.testing.HouseholdTestRig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class HouseholdLogicTest {


  private HouseholdTestRig householdTestRig;
  private AppUserTestRig appUserTestRig;

  @BeforeEach
  void setUp() {
    householdTestRig = new HouseholdTestRig();
    appUserTestRig = new AppUserTestRig();
  }

  @Test
  void shouldCreate_successfullyWithAdmin() {
    var userId = UUID.randomUUID();
    var createdHousehold = householdTestRig.logic().create(
        new UserContext(userId),
        "Test household"
    );

    assertThat(createdHousehold).isNotNull();
    assertThat(createdHousehold.id()).isNotNull();

    var adminMember = householdTestRig.memberRepository().findByMemberId(userId);

    assertThat(createdHousehold.name()).isEqualTo("Test household");
    assertThat(adminMember.isPresent()).isTrue();
    assertThat(adminMember.get().appUserId()).isEqualTo(userId);
    assertThat(adminMember.get().role()).isEqualTo(Role.ADMIN);
  }

  @Test
  void shouldGet_successfully_whenUserIsMember() {
    var userId = UUID.randomUUID();
    var userContext = new UserContext(userId);

    var createdHousehold = householdTestRig.logic().create(
        userContext,
        "Test household"
    );

    var fetchedHousehold = householdTestRig.logic().get(userContext, createdHousehold.id());

    assertThat(fetchedHousehold).isNotNull();
    assertThat(fetchedHousehold.name()).isEqualTo("Test household");
    assertThat(fetchedHousehold.createdBy()).isEqualTo(userId);
    assertThat(fetchedHousehold.isActive()).isTrue();
  }

  @Test
  void shouldThrow_whenGetting_deletedHousehold() {
    var user = appUserTestRig.createPersistedUser("test@zambone.dev");
    var deletedHousehold = householdTestRig
        .createDeletedHousehold("Deleted Household", user)
        .withDeletedAt(Instant.now());

    assertThrows(ResourceNotFoundException.class, () -> {
      householdTestRig.logic().get(new UserContext(user.id()), deletedHousehold.id());
    });
  }

  @Test
  void shouldThrow_whenGettingHousehold_ifUserIsNotMember() {
    var ownerId = UUID.randomUUID();
    var intruderId = UUID.randomUUID();

    var createdHousehold = householdTestRig.logic().create(
        new UserContext(ownerId),
        "Secret household"
    );

    assertThrows(ResourceNotFoundException.class, () -> {
      householdTestRig.logic().get(new UserContext(intruderId), createdHousehold.id());
    });
  }

  @Test
  void shouldUpdate_successfully_whenVersionMatches() {
    var userContext = new UserContext(UUID.randomUUID());
    var createdHousehold = householdTestRig.logic().create(
        userContext,
        "Update household"
    );
    var newName = "Updated Palace";
    var updatedHousehold = householdTestRig.logic().update(
        userContext,
        createdHousehold.id(),
        newName,
        createdHousehold.updatedAt()
    );

    assertThat(updatedHousehold).isNotNull();

    var storedHousehold = householdTestRig.repository().findByIdAndUserId(createdHousehold.id(), userContext.actorId());
    assertThat(storedHousehold.isPresent()).isTrue();
    assertThat(storedHousehold.get().name()).isEqualTo(newName);
  }

  @Test
  void shouldThrow_whenUpdating_withStaleVersion() {
    var userContext = new UserContext(UUID.randomUUID());
    var createdHousehold = householdTestRig.logic().create(
        userContext,
        "Update household"
    );
    var staleVersion = createdHousehold.updatedAt().minusSeconds(10);

    assertThrows(ConcurrentModificationException.class, () -> {
      householdTestRig.logic().update(
          userContext,
          createdHousehold.id(),
          "Hacker name",
          staleVersion
      );
    });
  }

  @Test
  void shouldDelete_successfully_whenVersionMatches() {
    var userContext = new UserContext(UUID.randomUUID());
    var createdHousehold = householdTestRig.logic().create(
        userContext,
        "Delete household"
    );
    var deletedHousehold = householdTestRig.logic().delete(
        userContext,
        createdHousehold.id(),
        createdHousehold.updatedAt());

    assertThat(deletedHousehold).isNotNull();

    var stored = householdTestRig.repository().findByIdAndUserId(deletedHousehold.id(), userContext.actorId());
    assertThat(stored.isPresent()).isTrue();
    assertThat(stored.get().deletedAt()).isNotNull();
    assertThat(stored.get().deletedAt()).isEqualTo(stored.get().updatedAt());
    assertThat(stored.get().deletedAt()).isEqualTo(deletedHousehold.deletedAt());
  }
}
