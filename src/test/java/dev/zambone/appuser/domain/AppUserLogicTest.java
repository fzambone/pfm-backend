package dev.zambone.appuser.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;
import dev.zambone.appuser.testing.AppUserTestRig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class AppUserLogicTest {

  private AppUserTestRig rig;

  @BeforeEach
  void setUp() {
    rig = new AppUserTestRig();
  }

  @Test
  void shouldCreate_successfully() {
    UserContext context = new UserContext(UUID.randomUUID());
    var createdUser =  rig.logic().create(
        context,
        "test@zambone.dev",
        "secret123",
        "Test User"
    );

    // Verify structure
    assertThat(createdUser).isNotNull();
    assertThat(createdUser.id()).isNotNull();

    // Verify data integrity
    assertThat(createdUser.email()).isEqualTo("test@zambone.dev");
    assertThat(createdUser.fullName()).isEqualTo("Test User");

    // Check logic
    assertThat(createdUser.passwordHash()).isEqualTo("secret123_hashed_for_test");
    assertThat(createdUser.passwordHash()).isNotEqualTo("secret123");

    // Check state
    var savedUser = rig.repository().findByEmail("test@zambone.dev");
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.get()).isEqualTo(createdUser);
  }

  @Test
  void shouldSetCreatedBy_fromContext() {
    UserContext context = new UserContext(UUID.randomUUID());
    var createdUser = rig.logic().create(
        context,
        "test@zambone.dev",
        "secret123",
        "Test Context user"
    );

    // Verify auditing trail user
    assertThat(createdUser.createdBy()).isEqualTo(context.actorId());
  }

  @Test
  void shouldThrow_whenCreating_withDuplicateEmail() {
    UserContext context = new UserContext(UUID.randomUUID());
    rig.createPersistedUser("duplicate@zambone.dev");
    assertThrows(UserAlreadyExistsException.class, () -> rig.logic().create(context, "duplicate@zambone.dev", "secret_123", "zambone"));
  }

  @Test
  void shouldReactivate_whenUserWasDeleted() {
    UserContext context = new UserContext(UUID.randomUUID());
    rig.createDeletedUser("reactivate@zambone.dev");
    var reactivatedUser = rig.logic().create(context, "reactivate@zambone.dev", "new_secret_123", "Reactivated User");
    assertThat(reactivatedUser).isNotNull();
    assertThat(reactivatedUser.deletedAt()).isNull();
    assertThat(reactivatedUser.fullName()).isEqualTo("Reactivated User");
    assertThat(reactivatedUser.passwordHash()).isEqualTo("new_secret_123_hashed_for_test");
  }

  @Test
  void shouldGet_whenUser_isAuthorized() {
    var user = rig.createPersistedUser("get@zambone.dev");
    rig.logic().get(new UserContext(user.id()), user.id());

    assertThat(user).isNotNull();
  }

  @Test
  void shouldThrow_whenGetting_deletedUser() {
    var deletedUser = rig.createDeletedUser("deleted@zambone.dev");

    assertThrows(ResourceNotFoundException.class, () -> {
      rig.logic().get(new UserContext(deletedUser.id()), deletedUser.id());
    });
  }

  @Test
  void shouldThrow_whenGetting_differentUser() {
    var deletedUser = rig.createDeletedUser("delete@zambone.dev");

    assertThrows(ResourceNotFoundException.class, () -> {
      rig.logic().get(new UserContext(UUID.randomUUID()), deletedUser.id());
    });
  }

}
