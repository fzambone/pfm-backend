package dev.zambone.household.domain;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import dev.zambone.appuser.domain.UserContext;
import dev.zambone.appuser.testing.AppUserTestRig;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.household.testing.HouseholdTestRig;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HouseholdLogicTest {

    private HouseholdTestRig householdTestRig;
    private AppUserTestRig appUserTestRig;

    @BeforeEach
    void setUp() {
        householdTestRig = new HouseholdTestRig();
        appUserTestRig = new AppUserTestRig();
    }

    @Test
    void shouldCreate_successfullyWithAdmin() {
        // Arrange
        var userId = UUID.randomUUID();

        // Act
        var createdHousehold =
                householdTestRig.logic().create(new UserContext(userId), "Test household");
        var adminMember = householdTestRig.memberRepository().findByMemberId(userId);

        // Assert
        assertThat(createdHousehold).isNotNull();
        assertThat(createdHousehold.id()).isNotNull();
        assertThat(createdHousehold.name()).isEqualTo("Test household");
        assertThat(adminMember.isPresent()).isTrue();
        assertThat(adminMember.get().appUserId()).isEqualTo(userId);
        assertThat(adminMember.get().role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldGet_successfully_whenUserIsMember() {
        // Arrange
        var userId = UUID.randomUUID();
        var userContext = new UserContext(userId);
        var createdHousehold = householdTestRig.logic().create(userContext, "Test household");

        // Act
        var fetchedHousehold = householdTestRig.logic().get(userContext, createdHousehold.id());

        // Assert
        assertThat(fetchedHousehold).isNotNull();
        assertThat(fetchedHousehold.name()).isEqualTo("Test household");
        assertThat(fetchedHousehold.createdBy()).isEqualTo(userId);
        assertThat(fetchedHousehold.isActive()).isTrue();
    }

    @Test
    void shouldThrow_whenGetting_deletedHousehold() {
        // Arrange
        var user = appUserTestRig.createPersistedUser("test@zambone.dev");
        var userContext = new UserContext(user.id());
        var logic = householdTestRig.logic();

        // Act
        var deletedHousehold =
                householdTestRig
                        .createDeletedHousehold("Deleted Household", user)
                        .withDeletedAt(Instant.now());
        var deletedHouseholdId = deletedHousehold.id();

        // Assert
        assertThrows(
                ResourceNotFoundException.class, () -> logic.get(userContext, deletedHouseholdId));
    }

    @Test
    void shouldThrow_whenGettingHousehold_ifUserIsNotMember() {
        // Arrange
        var ownerId = UUID.randomUUID();
        var userContext = new UserContext(UUID.randomUUID());
        var logic = householdTestRig.logic();

        // Act
        var createdHousehold = logic.create(new UserContext(ownerId), "Secret household");
        var createdHouseholdId = createdHousehold.id();

        // Assert
        assertThrows(
                ResourceNotFoundException.class, () -> logic.get(userContext, createdHouseholdId));
    }

    @Test
    void shouldUpdate_successfully_whenVersionMatches() {
        // Arrange
        var userContext = new UserContext(UUID.randomUUID());
        var createdHousehold = householdTestRig.logic().create(userContext, "Update household");
        var newName = "Updated Palace";

        // Act
        var updatedHousehold =
                householdTestRig
                        .logic()
                        .update(
                                userContext,
                                createdHousehold.id(),
                                newName,
                                createdHousehold.updatedAt());
        var storedHousehold = householdTestRig.logic().get(userContext, createdHousehold.id());

        // Assert
        assertThat(updatedHousehold).isNotNull();
        assertThat(storedHousehold).isNotNull();
        assertThat(storedHousehold.name()).isEqualTo(newName);
    }

    @Test
    void shouldThrow_whenUpdating_withStaleVersion() {
        // Arrange
        var userContext = new UserContext(UUID.randomUUID());
        var createdHousehold = householdTestRig.logic().create(userContext, "Update household");
        var logic = householdTestRig.logic();

        // Act
        var staleVersion = createdHousehold.updatedAt().minusSeconds(10);
        var createdHouseholdId = createdHousehold.id();

        // Assert
        assertThrows(
                ConcurrentModificationException.class,
                () -> logic.update(userContext, createdHouseholdId, "Hacker name", staleVersion));
    }

    @Test
    void shouldDelete_successfully_whenVersionMatches() {
        var userContext = new UserContext(UUID.randomUUID());
        var createdHousehold = householdTestRig.logic().create(userContext, "Delete household");
        var logic = householdTestRig.logic();

        // Act
        var deletedHousehold =
                logic.delete(userContext, createdHousehold.id(), createdHousehold.updatedAt());
        var deletedHouseholdId = deletedHousehold.id();

        // Assert
        assertThat(deletedHousehold).isNotNull();
        assertThat(deletedHousehold.deletedAt()).isNotNull();
        assertThrows(
                ResourceNotFoundException.class, () -> logic.get(userContext, deletedHouseholdId));
    }
}
