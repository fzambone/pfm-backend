package dev.zambone.appuser.testing;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.domain.AppUserLogic;
import dev.zambone.appuser.domain.PasswordHasher;

public class AppUserTestRig {

  private final FakeAppUserRepository repository = new FakeAppUserRepository();
  private final AppUserFactory factory = new AppUserFactory(repository);
  private final AppUserLogic logic;

  public AppUserTestRig() {
    PasswordHasher fakePasswordHasher = raw -> raw + "_hashed_for_test";
    this.logic = new AppUserLogic(repository, fakePasswordHasher);
  }

  public AppUser createPersistedUser(String email) {
    return factory.createAndPersistUser(email);
  }

  public AppUser createDeletedUser(String email) {
    return factory.createAndPersistDeletedUser(email);
  }

  public AppUserLogic logic() {
    return logic;
  }

  public FakeAppUserRepository repository() {
    return repository;
  }
}
