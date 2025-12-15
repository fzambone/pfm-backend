package dev.zambone.appusers.testing;

import dev.zambone.appusers.domain.AppUser;
import dev.zambone.appusers.domain.AppUserRepository;
import dev.zambone.appusers.domain.UserContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class AppUserFactory {

  private AppUserFactory() {}

  public static AppUser createAndPersistUser(AppUserRepository repository, String email) {
    var randomUserId = UUID.randomUUID();
    var user = new AppUser(
        randomUserId,
        email,
        "hashed_secret",
        "Test User",
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        randomUserId,
        randomUserId
    );

    return repository.save(user);
  }
}
