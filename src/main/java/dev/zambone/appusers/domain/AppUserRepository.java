package dev.zambone.appusers.domain;

import java.util.Optional;

public interface AppUserRepository {

  Optional<AppUser> findByEmail(String email);

  AppUser save(AppUser user);

}
