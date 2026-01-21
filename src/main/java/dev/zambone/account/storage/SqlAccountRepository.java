package dev.zambone.account.storage;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CurrencyCode;
import dev.zambone.common.exceptions.ResourceNotFoundException;
import java.sql.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(SqlAccountRepository.class);
    private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final DataSource dataSource;

    SqlAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Account save(Account account) {

        if (account.name().isBlank()) {
            throw new IllegalArgumentException("Account name can't be blank or null");
        }

        var sql =
                """
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
                    logger.info(
                            "Created new Account: [id={}, name={}]", account.id(), account.name());
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
    public Optional<Account> findById(UUID accountId, UUID userId) {
        var sql =
                """
        SELECT a.*
        FROM accounts a
        INNER JOIN household_members hm ON a.household_id = hm.household_id
        WHERE a.id = ?
            AND hm.app_user_id = ?
            AND a.deleted_at IS NULL
        """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            preparedStatement.setObject(i++, accountId);
            preparedStatement.setObject(i++, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    var foundAccount = new AccountMapper().map(resultSet);
                    logger.info(
                            "Found account: [id={}, name={}]",
                            foundAccount.id(),
                            foundAccount.name());
                    return Optional.of(foundAccount);
                }
                throw new ResourceNotFoundException("Account not found or access denied");
            }
        } catch (SQLException e) {
            logger.error("Error finding account by ID", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Account> findByName(UUID householdId, String name) {
        var sql =
                """
        SELECT a.*
        FROM accounts a
        WHERE a.household_id = ?
          AND LOWER(a.name) = LOWER(?)
          AND a.deleted_at IS NULL
        """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            preparedStatement.setObject(i++, householdId);
            preparedStatement.setString(i++, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    var foundAccount = new AccountMapper().map(resultSet);
                    logger.info(
                            "Found account: [id={}, name={}]",
                            foundAccount.id(),
                            foundAccount.name());
                    return Optional.of(foundAccount);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error finding account by Name", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Account account, Instant version) {
        var sql =
                """
        UPDATE accounts a
        SET name = ?, current_balance = ?, is_active = ?, updated_at = ?, updated_by = ?
        FROM household_members hm
        WHERE a.id = ?
          AND a.household_id = hm.household_id
          AND a.updated_at = ?
          AND a.deleted_at IS NULL
        """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            preparedStatement.setString(i++, account.name());
            preparedStatement.setBigDecimal(i++, account.currentBalance());
            preparedStatement.setBoolean(i++, account.isActive());
            preparedStatement.setTimestamp(i++, Timestamp.from(account.updatedAt()), UTC);
            preparedStatement.setObject(i++, account.updatedBy());
            preparedStatement.setObject(i++, account.id());
            preparedStatement.setTimestamp(i++, Timestamp.from(version), UTC);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Error updating Account: [id={}]", account.id());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Account account, Instant version) {
        var sql =
                """
        UPDATE accounts a
        SET deleted_at = ?
        FROM household_members hm
        WHERE a.id = ?
          AND a.household_id = hm.household_id
          AND a.updated_at = ?
          AND a.deleted_at IS NULL
        """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            preparedStatement.setTimestamp(i++, Timestamp.from(account.deletedAt()), UTC);
            preparedStatement.setObject(i++, account.id());
            preparedStatement.setTimestamp(i++, Timestamp.from(version), UTC);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Error deleting Account: [id={}]", account.id());
            throw new RuntimeException(e);
        }
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
                    resultSet.getTimestamp("deleted_at") != null
                            ? resultSet.getTimestamp("deleted_at").toInstant()
                            : null,
                    UUID.fromString(resultSet.getString("created_by")),
                    UUID.fromString(resultSet.getString("updated_by")));
        }
    }
}
