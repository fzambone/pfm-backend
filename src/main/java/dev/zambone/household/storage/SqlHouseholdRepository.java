package dev.zambone.household.storage;

import dev.zambone.household.domain.Household;
import dev.zambone.household.domain.HouseholdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

public class SqlHouseholdRepository implements HouseholdRepository {

  private static final Logger logger = LoggerFactory.getLogger(SqlHouseholdRepository.class);
  private final DataSource dataSource;
  private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

  public SqlHouseholdRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Optional<Household> findByIdAndUserId(UUID householdId, UUID userId) {

    var sql = """
        SELECT * FROM households h
        INNER JOIN household_members hm ON h.id = hm.household_id
        WHERE h.id = ?
          AND hm.app_user_id = ?
        """;

    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      preparedStatement.setObject(1, householdId);
      preparedStatement.setObject(2, userId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var foundHousehold = new HouseholdMapper().map(resultSet);
          logger.debug("Fetched household by Id: [id={}, name={}]", foundHousehold.id(), foundHousehold.name());
          return Optional.of(foundHousehold);
        }
      }
      throw new IllegalArgumentException("Household not found or access denied");
    } catch (SQLException e) {
      logger.error("Error fetching Household by Id", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Household save(Household household) {

    var sql = """
        INSERT INTO households (id, name, is_active, created_at, updated_at, created_by, updated_by)
          VALUES (?, ?, ?, ?, ?, ?, ?)
          RETURNING *
    """;

    try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setObject(i++, household.id());
      preparedStatement.setString(i++, household.name());
      preparedStatement.setBoolean(i++, household.isActive());
      preparedStatement.setTimestamp(i++, Timestamp.from(household.createdAt()), UTC);
      preparedStatement.setTimestamp(i++,Timestamp.from(household.updatedAt()), UTC);
      preparedStatement.setObject(i++, household.createdBy());
      preparedStatement.setObject(i++, household.updatedBy());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var savedHousehold = new HouseholdMapper().map(resultSet);
          logger.info("Created new Household: [id={}, name={}]", savedHousehold.id(), savedHousehold.name());
          return savedHousehold;
        }
        throw new SQLException("Creating Household failed, no rows affected.");
      }

    } catch (SQLException e) {
      logger.error("Error saving new Household", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean update(Household household, Instant version) {
    var sql = """
        UPDATE households
        SET name = ?, updated_at = ?, updated_by = ?
        WHERE id = ? AND updated_at = ?
        """;

    System.out.println("household: " + Timestamp.from(household.updatedAt()) + " version: " + version);

    try (Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setString(i++, household.name());
      preparedStatement.setTimestamp(i++, Timestamp.from(Instant.now()), UTC);
      preparedStatement.setObject(i++, household.updatedBy());
      preparedStatement.setObject(i++, household.id());
      preparedStatement.setTimestamp(i++, Timestamp.from(version), UTC);

      return preparedStatement.executeUpdate() > 0;

    } catch (SQLException e) {
      logger.error("Error updating Household: [id={}]", household.id());
      throw new RuntimeException(e);
    }
  }

  private static class HouseholdMapper {

    public Household map(ResultSet resultSet) throws SQLException {
      return new Household(
          UUID.fromString(resultSet.getString("id")),
          resultSet.getString("name"),
          resultSet.getBoolean("is_active"),
          resultSet.getTimestamp("created_at").toInstant(),
          resultSet.getTimestamp("updated_at").toInstant(),
          resultSet.getString("created_by") != null ? UUID.fromString(resultSet.getString("created_by")) : null,
          resultSet.getString("updated_by") != null ? UUID.fromString(resultSet.getString("updated_by")) : null
      );
    }
  }
}
