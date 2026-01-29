package dev.zambone.account.testing;

import dev.zambone.account.domain.Account;
import dev.zambone.account.domain.AccountLogic;
import dev.zambone.account.domain.AccountType;
import java.util.UUID;

public class AccountTestRig {

    private final FakeAccountRepository repository = new FakeAccountRepository();
    private final AccountFactory factory = new AccountFactory(repository);
    private final AccountLogic logic;

    public AccountTestRig() {
        this.logic = new AccountLogic(repository);
    }

    public Account createPersistedAccount(String name, UUID householdId, UUID userId) {
        return factory.createAndPersistAccount(name, householdId, userId);
    }

    public Account createPersistedAccount(
            String name, UUID householdId, UUID userId, AccountType accountType) {
        return factory.createAndPersistAccount(name, householdId, userId, accountType);
    }

    public Account createDeletedAccount(String name, UUID householdId, UUID userId) {
        return factory.createAndPersistDeletedAccount(name, householdId, userId);
    }

    public AccountLogic logic() {
        return logic;
    }

    public FakeAccountRepository repository() {
        return repository;
    }
}
