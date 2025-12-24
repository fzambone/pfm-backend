package dev.zambone.account.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;
import dev.zambone.account.testing.AccountTestRig;
import dev.zambone.appuser.domain.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class AccountLogicTest {

  private AccountTestRig rig;

  @BeforeEach
  void setUp() {
    rig = new AccountTestRig();
  }

  @Test
  void shouldCreate_whenAuthorized() {
    var userId = UUID.randomUUID();
    var householdId = UUID.randomUUID();

    var context = new UserContext(userId, Set.of(householdId));
    var createdAccount = rig.logic().create(
        householdId,
        "test account",
        AccountType.CHECKING,
        CurrencyCode.USD,
        context
    );
    assertThat(createdAccount).isNotNull();
  }

  @Test
  void shouldThrow_whenCreating_withoutAuthorization() {
    var householdId = UUID.randomUUID();
    var otherHouseholdId = UUID.randomUUID();
    var context = new UserContext(UUID.randomUUID(), Set.of(otherHouseholdId));

    assertThrows(ResourceNotFoundException.class, () ->
        rig.logic().create(householdId, "Test Account", AccountType.CHECKING, CurrencyCode.USD, context));
  }

  // TODO: shouldThrow_whenCreating_duplicatedNameInHousehold

  // TODO: shouldThrow_whenCreating_withEmptyName

  @Test
  void shouldGet_whenAuthorized() {
    var myHouseholdId = UUID.randomUUID();
    var context = new UserContext(UUID.randomUUID(), Set.of(myHouseholdId));
    var myAccount = rig.createPersistedAccount("Test Account", myHouseholdId);
    var fetched = rig.logic().get(context, myAccount.id());

    assertThat(fetched).isEqualTo(myAccount);
  }

  @Test
  void shouldThrow_whenGetting_withoutAuthorization() {
    var account = rig.createPersistedAccount("Test Account", UUID.randomUUID());
    var context = new UserContext(UUID.randomUUID());

    assertThrows(IllegalArgumentException.class, () ->
        rig.logic().get(context, account.id())
    );
  }

  @Test
  void shouldThrow_whenGetting_deletedAccount() {
    var householdId = UUID.randomUUID();
    var account = rig.createDeletedAccount("Deleted Account", householdId);
    var context = new UserContext(UUID.randomUUID(), Set.of(householdId));

    assertThrows(ResourceNotFoundException.class, () ->
        rig.logic().get(context, account.id()));
  }

  @Test
  void shouldUpdate_successfully_whenAuthorizedAndVersionMatches() {
    var householdId = UUID.randomUUID();
    var context = new UserContext(UUID.randomUUID(), Set.of(householdId));
    var account = rig.createPersistedAccount("Test Account", householdId);
    var newName = "Updated Account";
    var updatedAccount = rig.logic().update(account.id(), newName, context, account.updatedAt());

    assertThat(updatedAccount).isNotNull();

    var storedAccount = rig.repository().findById(updatedAccount.id());
    assertThat(storedAccount.isPresent()).isTrue();
    assertThat(storedAccount.get().name()).isEqualTo("Updated Account");
  }

  @Test
  void shouldThrow_whenUpdatingWithoutAuthorization() {
    var account = rig.createPersistedAccount("Test Account", UUID.randomUUID());
    var context = new UserContext(UUID.randomUUID(), Set.of(UUID.randomUUID()));

    assertThrows(IllegalArgumentException.class, () ->
        rig.logic().update(account.id(), "Updated Test Account", context, account.updatedAt()));
  }

  @Test
  void shouldThrow_whenUpdating_withStaleVersion() {
    var householdId = UUID.randomUUID();
    var account = rig.createPersistedAccount("Test Account", householdId);
    var context = new UserContext(UUID.randomUUID(), Set.of(householdId));

    assertThrows(ConcurrentModificationException.class, () ->
        rig.logic().update(account.id(), "Updated Test Account", context, Instant.now().minus(1000, ChronoUnit.SECONDS)));
  }

  @Test
  void shouldDelete_successfully_whenAuthorizedAndVersionMatches() {
    var householdId = UUID.randomUUID();
    var context = new UserContext(UUID.randomUUID(), Set.of(householdId));
    var account = rig.createPersistedAccount("Delete Account", householdId);
    var deletedAccount = rig.logic().delete(account.id(), context, account.updatedAt());

    assertThat(deletedAccount.deletedAt()).isNotNull();
  }

  @Test
  void shouldThrow_whenDeletingWithoutAuthorization() {
    var account = rig.createPersistedAccount("Test Account", UUID.randomUUID());
    var context = new UserContext(UUID.randomUUID(), Set.of(UUID.randomUUID()));

    assertThrows(IllegalArgumentException.class, () ->
        rig.logic().delete(account.id(), context, account.updatedAt()));
  }

  @Test
  void shouldThrow_whenDeleting_withStaleVersion() {
    var householdId = UUID.randomUUID();
    var account = rig.createPersistedAccount("Test Account", householdId);
    var context = new UserContext(UUID.randomUUID(), Set.of(householdId));

    assertThrows(ConcurrentModificationException.class, () ->
        rig.logic().delete(account.id(), context, Instant.now().minus(1000, ChronoUnit.SECONDS)));
  }
}
