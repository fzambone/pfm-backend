package dev.zambone.account.testing;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.AccountType;
import dev.zambone.account.domain.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AccountFactory {

    private final AccountRepository repository;

    public AccountFactory(AccountRepository repository) {
        this.repository = repository;
    }

    public Account createAndPersistAccount(String name, UUID householdId, UUID userId) {
        var account =
                new Account(
                        UUID.randomUUID(),
                        householdId,
                        name,
                        AccountType.CHECKING,
                        CurrencyCode.USD,
                        BigDecimal.ZERO,
                        true,
                        Instant.now(),
                        Instant.now(),
                        null,
                        userId,
                        userId);
        return repository.save(account);
    }

    public Account createAndPersistAccount(
            String name, UUID householdId, UUID userId, AccountType accountType) {
        var account =
                new Account(
                        UUID.randomUUID(),
                        householdId,
                        name,
                        accountType,
                        CurrencyCode.USD,
                        BigDecimal.ZERO,
                        true,
                        Instant.now(),
                        Instant.now(),
                        null,
                        userId,
                        userId);
        return repository.save(account);
    }

    public Account createAndPersistDeletedAccount(String name, UUID householdId, UUID userId) {
        var account =
                new Account(
                        UUID.randomUUID(),
                        householdId,
                        name,
                        AccountType.CHECKING,
                        CurrencyCode.USD,
                        BigDecimal.ZERO,
                        true,
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        userId,
                        userId);
        return repository.save(account);
    }
}
