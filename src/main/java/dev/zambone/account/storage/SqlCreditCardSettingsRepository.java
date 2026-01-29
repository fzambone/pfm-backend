package dev.zambone.account.storage;

import dev.zambone.account.domain.CreditCardSettings;
import dev.zambone.account.domain.CreditCardSettingsRepository;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import dev.zambone.common.messages.ErrorMessages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlCreditCardSettingsRepository implements CreditCardSettingsRepository {

  private static final Logger logger = LoggerFactory.getLogger(
      SqlCreditCardSettingsRepository.class);
  private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  private final DataSource dataSource;

  SqlCreditCardSettingsRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public CreditCardSettings save(CreditCardSettings creditCardSettings) {
    var sql = """
        INSERT INTO credit_card_settings (id, account_id, closing_day, due_day, 
        limit_amount, is_active, created_at, updated_at, deleted_at, created_by, updated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING *
        """;

    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setObject(i++, creditCardSettings.id());
      preparedStatement.setObject(i++, creditCardSettings.accountId());
      preparedStatement.setInt(i++, creditCardSettings.closingDay());
      preparedStatement.setInt(i++, creditCardSettings.dueDay());
      preparedStatement.setBigDecimal(i++, creditCardSettings.limitAmount());
      preparedStatement.setBoolean(i++, creditCardSettings.isActive());
      preparedStatement.setTimestamp(i++, Timestamp.from(creditCardSettings.createdAt()), UTC);
      preparedStatement.setTimestamp(i++, Timestamp.from(creditCardSettings.updatedAt()), UTC);
      preparedStatement.setNull(i++, Types.TIMESTAMP);
      preparedStatement.setObject(i++, creditCardSettings.createdBy());
      preparedStatement.setObject(i++, creditCardSettings.updatedBy());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var savedCreditCardSettings = new CreditCardSettingsMapper().map(resultSet);
          logger.info(
              "Created new Credit Card Settings: [id={}, account id={}]", creditCardSettings.id(),
              creditCardSettings.accountId());
          return savedCreditCardSettings;
        }
        throw new SQLException("Creating Credit Card Settings failed, no rows affected.");
      }

    } catch (SQLException e) {
      logger.error("Error saving new Credit Card Settings", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<CreditCardSettings> findById(UUID creditCardSettingsId) {
    return Optional.empty();
  }

  @Override
  public Optional<CreditCardSettings> findByAccountId(UUID accountId) {
    var sql = """
        SELECT cc.*
        FROM credit_card_settings cc
        WHERE cc.account_id = ?
            AND cc.deleted_at IS NULL 
        """;

    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      int i = 1;
      preparedStatement.setObject(i++, accountId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var foundCcSettings = new CreditCardSettingsMapper().map(resultSet);
          logger.info("Found Credit Card Settings for account: [id={}]",
              foundCcSettings.accountId());
          return Optional.of(foundCcSettings);
        }
        throw new ResourceNotFoundException(ErrorMessages.creditCardSettingsNotFound(accountId));
      }
    } catch (SQLException e) {
      logger.error("Error finding Credit Card Setting by Account Id", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean update(CreditCardSettings ccSettings, Instant version) {
    var sql = """
        UPDATE credit_card_settings cc
        SET closing_day = ?, due_day = ?, limit_amount = ?,
        updated_at = ?, updated_by = ?
        WHERE cc.id = ?
          AND cc.updated_at = ?
          AND cc.deleted_at IS NULL
        """;

    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      int i = 1;
      preparedStatement.setInt(i++, ccSettings.closingDay());
      preparedStatement.setInt(i++, ccSettings.dueDay());
      preparedStatement.setBigDecimal(i++, ccSettings.limitAmount());
      preparedStatement.setTimestamp(i++, Timestamp.from(ccSettings.updatedAt()), UTC);
      preparedStatement.setObject(i++, ccSettings.updatedBy());
      preparedStatement.setObject(i++, ccSettings.id());
      preparedStatement.setTimestamp(i++, Timestamp.from(version), UTC);

      logger.info("Updated Credit Card Settings for Account: [id={}] successfully",
          ccSettings.accountId());
      return preparedStatement.executeUpdate() > 0;

    } catch (SQLException e) {
      logger.error("Error updating Credit Card Settings for Account: [id={}]",
          ccSettings.accountId());
      throw new RuntimeException(e);
    }
  }

  private static class CreditCardSettingsMapper {

    public CreditCardSettings map(ResultSet resultSet) throws SQLException {
      return new CreditCardSettings(
          UUID.fromString(resultSet.getString("id")),
          UUID.fromString(resultSet.getString("account_id")),
          resultSet.getInt("closing_day"),
          resultSet.getInt("due_day"),
          resultSet.getBigDecimal("limit_amount"),
          resultSet.getBoolean("is_active"),
          resultSet.getTimestamp("created_at").toInstant(),
          resultSet.getTimestamp("updated_at").toInstant(),
          resultSet.getTimestamp("deleted_at") != null
              ? resultSet.getTimestamp("deleted_at").toInstant()
              : null,
          UUID.fromString(resultSet.getString("created_by")),
          UUID.fromString(resultSet.getString("updated_by")));
    }
  }
}
