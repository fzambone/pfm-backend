package dev.zambone.appusers.domain;

import java.time.Instant;
import java.util.UUID;

public class AppUserLogic {

  private final AppUserRepository repository;
  private final PasswordHasher passwordHasher;

  public AppUserLogic(AppUserRepository repository, PasswordHasher passwordHasher) {
    this.repository = repository;
    this.passwordHasher = passwordHasher;
  }

  public AppUser createUser(UserContext context, String email, String rawPassword, String fullName) {
    var user = new AppUser(UUID.randomUUID(), email, passwordHasher.hash(rawPassword), fullName, Instant.now(), Instant.now(), context.actorId(), context.actorId());

    return repository.save(user);
  }
}
