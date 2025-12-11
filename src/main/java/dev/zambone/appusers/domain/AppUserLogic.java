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

  public AppUser createUser(String email, String rawPassword, String fullName) {

    // TODO: Implement user context to fetch creator ID
    var user = new AppUser(UUID.randomUUID(), email, passwordHasher.hash(rawPassword), fullName, Instant.now(), null);

    return repository.save(user);
  }
}
