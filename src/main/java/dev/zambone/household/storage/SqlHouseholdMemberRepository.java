package dev.zambone.household.storage;

import dev.zambone.household.domain.HouseholdMember;
import dev.zambone.household.domain.HouseholdMemberRepository;
import dev.zambone.household.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SqlHouseholdMemberRepository implements HouseholdMemberRepository {

  private static final Logger logger = LoggerFactory.getLogger(SqlHouseholdMemberRepository.class);
  private final DataSource dataSource;

  public SqlHouseholdMemberRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public HouseholdMember save(HouseholdMember householdMember) {
    var sql = """
        INSERT INTO household_members (household_id, app_user_id, role, joined_at, invited_by)
        VALUES (?, ?, ?, ?, ?)
        RETURNING *
        """;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setObject(i++, householdMember.householdId());
      preparedStatement.setObject(i++, householdMember.appUserId());
      preparedStatement.setString(i++, householdMember.role().toString());
      preparedStatement.setTimestamp(i++, Timestamp.from(householdMember.joinedAt()));
      preparedStatement.setObject(i++, householdMember.invitedBy());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var savedHouseholdMember = new HouseholdMemberMapper().map(resultSet);
          logger.info("Created new Household Member");
          return  savedHouseholdMember;
        }
        throw new SQLException("Creating Household Member failed, no rows affected.");
      }

    } catch (SQLException e) {
      logger.error("Error saving new Household Member", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<HouseholdMember> findByMemberId(UUID householdMemberId) {
    var sql = """
        SELECT * FROM household_members
        WHERE app_user_id = ?
        """;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      preparedStatement.setObject(1, householdMemberId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var foundHouseholdMember = new HouseholdMemberMapper().map(resultSet);
          logger.debug("Fetched Household Member by Member Id: [Household Id={}, User Id={}]", foundHouseholdMember.householdId(), foundHouseholdMember.appUserId());
          return Optional.of(foundHouseholdMember);
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      logger.error("Error fetching Household Member by Id", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<HouseholdMember> findMembership(UUID householdId, UUID userId) {
    var sql = """
        SELECT * FROM household_members
        WHERE household_id = ?
        AND app_user_id = ?
        """;

    try (Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      preparedStatement.setObject(1, householdId);
      preparedStatement.setObject(2, userId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var foundHouseholdMember = new HouseholdMemberMapper().map(resultSet);
          logger.debug("Fetched Household Member for [Household Id={} and User Id ={}]", foundHouseholdMember.householdId(), foundHouseholdMember.appUserId());
          return Optional.of(foundHouseholdMember);
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      logger.error("Error fetching Household id [{}] for Member Id [{}]", householdId, userId, e);
      throw new RuntimeException(e);
    }
  }

  private static class HouseholdMemberMapper {

    public HouseholdMember map(ResultSet rs) throws SQLException {
      return new HouseholdMember(
          UUID.fromString(rs.getString("household_id")),
          UUID.fromString(rs.getString("app_user_id")),
          Role.valueOf(rs.getString("role")),
          rs.getTimestamp("joined_at").toInstant(),
          UUID.fromString(rs.getString("invited_by"))
      );
    }
  }
}
