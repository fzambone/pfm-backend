package dev.zambone.testing;


import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabase {

  private static final PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withReuse(true);

  private static DataSource dataSource;

  private TestDatabase() {}

  public static DataSource getDataSource() {
    if (dataSource == null) {
      dataSource = startContainerAndMigrate();
    }
    return dataSource;
  }

  public static void reset() {
    // Fast cleanup: truncate tables instead of restarting container
    try (Connection connection = getDataSource().getConnection()) {
      Statement statement = connection.createStatement();
      statement.execute("SET session_replication_role = 'replica'");
      statement.execute("TRUNCATE TABLE app_users, households, household_members RESTART IDENTITY CASCADE");
      statement.execute("SET session_replication_role = 'origin'");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static PGSimpleDataSource startContainerAndMigrate() {
    container.start();

    Flyway.configure()
        .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
        .load()
        .migrate();

    PGSimpleDataSource dataSource = new PGSimpleDataSource();

    dataSource.setServerNames(new String[]{container.getHost()});
    dataSource.setPortNumbers(new int[]{container.getFirstMappedPort()});
    dataSource.setDatabaseName(container.getDatabaseName());
    dataSource.setUser(container.getUsername());
    dataSource.setPassword(container.getPassword());

    return dataSource;
  }
}
