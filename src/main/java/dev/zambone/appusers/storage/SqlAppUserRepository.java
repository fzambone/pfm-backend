package dev.zambone.appusers.storage;

import dev.zambone.appusers.domain.AppUser;
import dev.zambone.appusers.domain.AppUserRepository;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SqlAppUserRepository implements AppUserRepository {

  private final Jdbi jdbi;

  public SqlAppUserRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Optional<AppUser> findByEmail(String email) {
    return jdbi.withHandle(handle -> {
      var sql = """
          SELECT * FROM app_users
          WHERE email = :email
          """;

      return handle.createQuery(sql)
          .bind("email", email)
          .map(new AppUserMapper())
          .findOne();
    });
  }

  @Override
  public AppUser save(AppUser user) {
    return jdbi.withHandle(handle -> {
      var sql = """
          INSERT INTO app_users (id, email, password_hash, full_name, created_at, created_by)
          VALUES (:id, :email, :passwordHash, :fullName, :createdAt, :createdBy)
          RETURNING *
          """;

      return handle.createUpdate(sql)
          .bind("id", user.id())
          .bind("email", user.email())
          .bind("passwordHash", user.passwordHash())
          .bind("fullName", user.fullName())
          .bind("createdAt", user.createdAt())
          .bind("createdBy", user.createdBy())
          .executeAndReturnGeneratedKeys()
          .map(new AppUserMapper())
          .one();
    });
  }

  private static class AppUserMapper implements RowMapper<AppUser> {

    @Override
    public AppUser map(ResultSet rs, StatementContext ctx) throws SQLException {
      return new AppUser(
          UUID.fromString(rs.getString("id")),
          rs.getString("email"),
          rs.getString("password_hash"),
          rs.getString("full_name"),
          rs.getTimestamp("created_at").toInstant(),
          rs.getTimestamp("created_by") != null ? rs.getTimestamp("created_by").toInstant() : null
      );
    }
  }
}
