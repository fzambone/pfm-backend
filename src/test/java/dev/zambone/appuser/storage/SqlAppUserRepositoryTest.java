package dev.zambone.appuser.storage;

import dev.zambone.appuser.testing.AppUserFactory;
import dev.zambone.testing.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.google.common.truth.Truth.assertThat;

public class SqlAppUserRepositoryTest {

  private SqlAppUserRepository repository;
  private AppUserFactory factory;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();
    TestDatabase.reset();
    repository = new SqlAppUserRepository(dataSource);
    factory = new AppUserFactory(repository);
  }

  @Test
  void shouldSaveAndFindUser() {
    var user = factory.createAndPersistUser("test@zambone.dev");
    var foundUser = repository.findByEmail("test@zambone.dev");

    assertThat(foundUser.isPresent()).isTrue();
    assertThat(foundUser.get()).isEqualTo(user);
  }
}
