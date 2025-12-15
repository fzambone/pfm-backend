package dev.zambone.appusers.domain;

import dev.zambone.appusers.testing.FakeAppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

public class AppUserLogicTest {

  private AppUserLogic logic;
  private FakeAppUserRepository fakeRepository;
  private UserContext context;

  @BeforeEach
  void setUp() {
    fakeRepository = new FakeAppUserRepository();
    PasswordHasher fakeHasher = raw -> raw + "_hashed_for_test";
    logic = new AppUserLogic(fakeRepository, fakeHasher);
    context = new UserContext(UUID.randomUUID());
  }

  @Test
  public void shouldCreateUser_successfully() {
    var createdUser =  logic.createUser(
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
    var savedUser = fakeRepository.findByEmail("test@zambone.dev");
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.get()).isEqualTo(createdUser);
  }

  @Test
  public void shouldSetCreatedBy_fromContext() {
    var createdUser = logic.createUser(
        context,
        "test@zambone.dev",
        "secret123",
        "Test Context user"
    );

    // Verify auditing trail user
    assertThat(createdUser.createdBy()).isEqualTo(context.actorId());
  }


}
