package dev.zambone.account.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

  Account save(Account account);

  Optional<Account> findById(UUID id);

  boolean update(Account account, Instant version);

  boolean delete(Account account, Instant version);
}
