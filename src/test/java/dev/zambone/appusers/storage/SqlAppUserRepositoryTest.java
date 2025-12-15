package dev.zambone.appusers.storage;

import dev.zambone.appusers.testing.AppUserFactory;
import dev.zambone.testing.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.google.common.truth.Truth.assertThat;

public class SqlAppUserRepositoryTest {

  private SqlAppUserRepository repository;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.getDataSource();

    TestDatabase.reset();

    repository = new SqlAppUserRepository(dataSource);
  }

  @Test
  void shouldSaveAndFindUser() {
    var user = AppUserFactory.createAndPersistUser(repository, "test@zambone.dev");
    var foundUser = repository.findByEmail("test@zambone.dev");

    assertThat(foundUser.isPresent()).isTrue();
    assertThat(foundUser.get()).isEqualTo(user);
  }
}
