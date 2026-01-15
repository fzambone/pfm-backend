package dev.zambone.account.storage;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CurrencyCode;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

public class SqlAccountRepository implements AccountRepository {

  private static final Logger logger = LoggerFactory.getLogger(SqlAccountRepository.class);
  private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  private final DataSource dataSource;

  SqlAccountRepository(DataSource dataSource) { this.dataSource = dataSource; }

  @Override
  public Account save(Account account) {
    var sql = """
        INSERT INTO accounts (id, household_id, name, account_type, currency_code, current_balance,
        is_active, created_at, updated_at, deleted_at, created_by, updated_by) VALUES
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING *
        """;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      int i = 1;
      preparedStatement.setObject(i++, account.id());
      preparedStatement.setObject(i++, account.householdId());
      preparedStatement.setString(i++, account.name());
      preparedStatement.setString(i++, account.type().toString());
      preparedStatement.setString(i++, account.currencyCode().toString());
      preparedStatement.setBigDecimal(i++, account.currentBalance());
      preparedStatement.setBoolean(i++, account.isActive());
      preparedStatement.setTimestamp(i++, Timestamp.from(account.createdAt()), UTC);
      preparedStatement.setTimestamp(i++, Timestamp.from(account.updatedAt()), UTC);
      preparedStatement.setNull(i++, Types.TIMESTAMP);
      preparedStatement.setObject(i++, account.createdBy());
      preparedStatement.setObject(i++, account.updatedBy());

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          var savedAccount = new AccountMapper().map(resultSet);
          logger.info("Created new Account: [id={}, name={}]", account.id(), account.name());
          return savedAccount;
        }
        throw new SQLException("Creating Account failed, no rows affected.");
      }

    } catch (SQLException e) {
      logger.error("Error saving new Account", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<Account> findById(UUID id) {
    return Optional.empty();
  }

  @Override
  public boolean update(Account account, Instant version) {
    return false;
  }

  @Override
  public boolean delete(Account account, Instant version) {
    return false;
  }

  private static class AccountMapper {

    public Account map(ResultSet resultSet) throws SQLException {
      return new Account(
          UUID.fromString(resultSet.getString("id")),
          UUID.fromString(resultSet.getString("household_id")),
          resultSet.getString("name"),
          AccountType.valueOf(resultSet.getString("account_type")),
          CurrencyCode.valueOf(resultSet.getString("currency_code")),
          resultSet.getBigDecimal("current_balance"),
          resultSet.getBoolean("is_active"),
          resultSet.getTimestamp("created_at").toInstant(),
          resultSet.getTimestamp("updated_at").toInstant(),
          resultSet.getTimestamp("deleted_at") != null ? resultSet.getTimestamp("deleted_at").toInstant() : null,
          UUID.fromString(resultSet.getString("created_by")),
          UUID.fromString(resultSet.getString("updated_by"))
      );
    }
  }
}
