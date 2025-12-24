package dev.zambone.appuser.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository {

  Optional<AppUser> findByEmail(String email);

  Optional<AppUser> findById(UUID id);

  AppUser save(AppUser user);

  boolean update(AppUser updatedUser, Instant version);
}
