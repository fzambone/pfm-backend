package dev.zambone.account.domain;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import dev.zambone.account.testing.AccountTestRig;
import dev.zambone.appuser.domain.UserContext;
import dev.zambone.common.exceptions.DuplicateResourceNameException;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountLogicTest {

    private AccountTestRig rig;

    @BeforeEach
    void setUp() {
        rig = new AccountTestRig();
    }

    @Test
    void shouldCreate_whenAuthorized() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var context = new UserContext(userId, Set.of(householdId));

        // Act
        var createdAccount =
                rig.logic()
                        .create(
                                householdId,
                                "test account",
                                AccountType.CHECKING,
                                CurrencyCode.USD,
                                context);

        // Assert
        assertThat(createdAccount).isNotNull();
    }

    @Test
    void shouldThrow_whenCreating_withoutAuthorization() {
        // Arrange
        var householdId = UUID.randomUUID();
        var otherHouseholdId = UUID.randomUUID();
        var context = new UserContext(UUID.randomUUID(), Set.of(otherHouseholdId));
        var logic = rig.logic();

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        logic.create(
                                householdId,
                                "Test Account",
                                AccountType.CHECKING,
                                CurrencyCode.USD,
                                context));
    }

    @Test
    void shouldThrow_whenCreating_duplicatedNameInHousehold() {
        // Arrange
        var householdId = UUID.randomUUID();
        var context = new UserContext(UUID.randomUUID(), Set.of(householdId));
        var logic = rig.logic();

        // Act
        logic.create(
                householdId, "test duplication", AccountType.CHECKING, CurrencyCode.USD, context);

        // Assert
        assertThrows(
                DuplicateResourceNameException.class,
                () ->
                        logic.create(
                                householdId,
                                "test duplication",
                                AccountType.CHECKING,
                                CurrencyCode.USD,
                                context));
    }

    @Test
    void shouldThrow_whenCreating_withEmptyName() {
        // Arrange
        var householdId = UUID.randomUUID();
        var context = new UserContext(UUID.randomUUID());
        var logic = rig.logic();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        logic.create(
                                householdId, "", AccountType.CHECKING, CurrencyCode.USD, context));
    }

    @Test
    void shouldGet_whenAuthorized() {
        // Arrange
        var myUserId = UUID.randomUUID();
        var myHouseholdId = UUID.randomUUID();
        var context = new UserContext(myUserId, Set.of(myHouseholdId));
        var myAccount = rig.createPersistedAccount("Test Account", myHouseholdId, myUserId);

        // Act
        var fetched = rig.logic().get(context, myAccount.id());

        // Assert
        assertThat(fetched).isEqualTo(myAccount);
    }

    @Test
    void shouldThrow_whenGetting_withoutAuthorization() {
        // Arrange
        var accountId =
                rig.createPersistedAccount("Test Account", UUID.randomUUID(), UUID.randomUUID())
                        .id();
        var context = new UserContext(UUID.randomUUID());
        var logic = rig.logic();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> logic.get(context, accountId));
    }

    @Test
    void shouldThrow_whenGetting_deletedAccount() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var accountId = rig.createDeletedAccount("Deleted Account", householdId, userId).id();
        var logic = rig.logic();
        var context = new UserContext(userId, Set.of(householdId));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> logic.get(context, accountId));
    }

    @Test
    void shouldUpdate_successfully_whenAuthorizedAndVersionMatches() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var context = new UserContext(userId, Set.of(householdId));
        var account = rig.createPersistedAccount("Test Account", householdId, userId);
        var newName = "Updated Account";

        // Act
        var updatedAccount =
                rig.logic().update(account.id(), newName, context, account.updatedAt());
        var storedAccount = rig.logic().get(context, account.id());

        // Assert
        assertThat(updatedAccount).isNotNull();
        assertThat(storedAccount).isNotNull();
        assertThat(storedAccount.name()).isEqualTo("Updated Account");
    }

    @Test
    void shouldThrow_whenUpdatingWithoutAuthorization() {
        // Arrange
        var account =
                rig.createPersistedAccount("Test Account", UUID.randomUUID(), UUID.randomUUID());
        var accountId = account.id();
        var updatedAt = Instant.now();
        var context = new UserContext(UUID.randomUUID(), Set.of(UUID.randomUUID()));
        var logic = rig.logic();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> logic.update(accountId, "Updated Test Account", context, updatedAt));
    }

    @Test
    void shouldThrow_whenUpdating_withStaleVersion() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var accountId = rig.createPersistedAccount("Test Account", householdId, userId).id();
        var logic = rig.logic();
        var context = new UserContext(userId, Set.of(householdId));
        var version = Instant.now().minus(1000, ChronoUnit.SECONDS);

        // Act & Assert
        assertThrows(
                ConcurrentModificationException.class,
                () -> logic.update(accountId, "Updated Test Account", context, version));
    }

    @Test
    void shouldDelete_successfully_whenAuthorizedAndVersionMatches() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var context = new UserContext(userId, Set.of(householdId));
        var account = rig.createPersistedAccount("Delete Account", householdId, userId);
        var logic = rig.logic();

        // Act
        var deletedAccount = logic.delete(account.id(), context, account.updatedAt());
        var deletedAccountId = deletedAccount.id();

        // Assert
        assertThat(deletedAccount.deletedAt()).isNotNull();
        assertThrows(ResourceNotFoundException.class, () -> logic.get(context, deletedAccountId));
    }

    @Test
    void shouldThrow_whenDeletingWithoutAuthorization() {
        // Arrange
        var userId = UUID.randomUUID();
        var accountId = rig.createPersistedAccount("Test Account", UUID.randomUUID(), userId).id();
        var version = Instant.now().minusSeconds(60);
        var context = new UserContext(userId, Set.of(UUID.randomUUID()));
        var logic = rig.logic();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class, () -> logic.delete(accountId, context, version));
    }

    @Test
    void shouldThrow_whenDeleting_withStaleVersion() {
        // Arrange
        var userId = UUID.randomUUID();
        var householdId = UUID.randomUUID();
        var accountId = rig.createPersistedAccount("Test Account", householdId, userId).id();
        var version = Instant.now().minusSeconds(1000);
        var logic = rig.logic();
        var context = new UserContext(userId, Set.of(householdId));

        // Act & Assert
        assertThrows(
                ConcurrentModificationException.class,
                () -> logic.delete(accountId, context, version));
    }
}
