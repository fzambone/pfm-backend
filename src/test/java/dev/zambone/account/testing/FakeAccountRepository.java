package dev.zambone.account.testing;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class FakeAccountRepository implements AccountRepository {

  private final HashMap<UUID, Account> table = new HashMap<>();

  @Override
  public Account save(Account account) {
    table.put(account.id(), account);
    return table.get(account.id());
  }

  @Override
  public Optional<Account> findById(UUID id) {
    return Optional.of(table.get(id));
  }

  @Override
  public boolean update(Account account, Instant version) {
    var existingAccount = table.get(account.id());

    if (existingAccount == null) return false;

    if (!existingAccount.updatedAt().equals(version)) {
      return false;
    }

    table.put(account.id(), account);
    return true;
  }

  @Override
  public boolean delete(Account account, Instant version) {
    var existingAccount = table.get(account.id());

    if (existingAccount == null) return false;

    if (!existingAccount.updatedAt().equals(version)) {
      return false;
    }

    table.remove(existingAccount.id());
    return true;
  }
}
