package dev.zambone.appusers.testing;

import dev.zambone.appusers.domain.AppUser;
import dev.zambone.appusers.domain.AppUserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeAppUserRepository implements AppUserRepository {

  private final Map<String, AppUser> table = new HashMap<>();

  @Override
  public AppUser save(AppUser user) {
    table.put(user.email(), user);
    return table.get(user.email());
  }

  @Override
  public Optional<AppUser> findByEmail(String email) {
      return Optional.ofNullable(table.get(email));
  }


}
