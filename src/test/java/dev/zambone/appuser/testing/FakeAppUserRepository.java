package dev.zambone.appuser.testing;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.domain.AppUserRepository;
import dev.zambone.appuser.domain.UserContext;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeAppUserRepository implements AppUserRepository {

  private final Map<String, AppUser> table = new HashMap<>();

  @Override
  public AppUser save(AppUser user) {
    table.put(user.email(), user);
    return table.get(user.email());
  }

  @Override
  public boolean update(AppUser updatedUser, Instant version) {
    var existingUser = table.get(updatedUser.email());

    if (existingUser == null) return false;

    if (!existingUser.updatedAt().equals(version)) {
      return false;
    }

    table.put(existingUser.email(), existingUser);
    return true;
  }

  @Override
  public Optional<AppUser> findByEmail(String email) {
      return Optional.ofNullable(table.get(email));
  }

  @Override
  public Optional<AppUser> findById(UUID id) {
    for (Map.Entry<String, AppUser> entry : table.entrySet()) {
      if (entry.getValue().id().equals(id) && entry.getValue().deletedAt() == null) {
        return Optional.of(entry.getValue());
      }
    }

    return Optional.empty();
  }
}
