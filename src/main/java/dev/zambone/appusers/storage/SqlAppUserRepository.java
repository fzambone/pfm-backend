package dev.zambone.appusers.storage;

import dev.zambone.appusers.domain.AppUser;
import dev.zambone.appusers.domain.AppUserRepository;
import dev.zambone.household.storage.SqlHouseholdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SqlAppUserRepository implements AppUserRepository {

  private static final Logger logger = LoggerFactory.getLogger(SqlHouseholdRepository.class);
  private final DataSource dataSource;

  public SqlAppUserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Optional<AppUser> findByEmail(String email) {

    var sql = """
        SELECT * FROM app_users
        WHERE email = ?
        """;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      preparedStatement.setString(1, email);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var foundAppUser = new AppUserMapper().map(resultSet);
          logger.debug("Fetched app user by Email: [id={}]", foundAppUser.id());
          return Optional.of(foundAppUser);
        }
      }
      return  Optional.empty();

    } catch (SQLException e) {
      logger.error("Error trying to fetch App User by Id", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public AppUser save(AppUser appUser) {

    var sql = """
        INSERT INTO app_users (id, email, password_hash, full_name, created_at, updated_at, created_by, updated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING *
        """;

    try (Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setObject(i++, appUser.id());
      preparedStatement.setString(i++, appUser.email());
      preparedStatement.setString(i++, appUser.passwordHash());
      preparedStatement.setString(i++, appUser.fullName());
      preparedStatement.setTimestamp(i++, Timestamp.from(appUser.createdAt()));
      preparedStatement.setTimestamp(i++, Timestamp.from(appUser.updatedAt()));
      preparedStatement.setObject(i++, appUser.createdBy());
      preparedStatement.setObject(i++, appUser.updatedBy());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var savedAppUser = new AppUserMapper().map(resultSet);
          logger.info("Created new App User: [id={}]", savedAppUser.id());
          return savedAppUser;
        }
        throw new SQLException("Creating App User failed, no rows affected.");
      }

    } catch (SQLException e) {
      logger.error("Error trying to save new App User", e);
      throw new RuntimeException(e);
    }
  }

  private static class AppUserMapper {

    public AppUser map(ResultSet resultSet) throws SQLException {
      return new AppUser(
          UUID.fromString(resultSet.getString("id")),
          resultSet.getString("email"),
          resultSet.getString("password_hash"),
          resultSet.getString("full_name"),
          resultSet.getTimestamp("created_at").toInstant(),
          resultSet.getTimestamp("updated_at").toInstant(),
          UUID.fromString(resultSet.getString("created_by")),
          UUID.fromString(resultSet.getString("updated_by"))
      );
    }
  }
}
