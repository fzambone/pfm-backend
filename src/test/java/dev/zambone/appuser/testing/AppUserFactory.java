package dev.zambone.appuser.testing;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.domain.AppUserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class AppUserFactory {

  private final AppUserRepository repository;

  public AppUserFactory(AppUserRepository repository) {
    this.repository = repository;
  }

  public AppUser createAndPersistUser(String email) {
    var user = createBaseUser(email);
    return repository.save(user);
  }

  public AppUser createAndPersistDeletedUser(String email) {
    var user = createBaseUser(email);
    var deletedUser = user.withDeletedAt(Instant.now());
    return repository.save(deletedUser);
  }

  private static AppUser createBaseUser(String email) {
    var randomUserId = UUID.randomUUID();
    return new AppUser(
        randomUserId,
        email,
        "hashed_secret",
        "Test User",
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        randomUserId,
        randomUserId
    );
  }

}
