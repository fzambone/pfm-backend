package dev.zambone.account.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID accountId, UUID userId);

    boolean update(Account account, Instant version);

    boolean delete(Account account, Instant version);

    Optional<Account> findByName(UUID householdId, String name);
}
