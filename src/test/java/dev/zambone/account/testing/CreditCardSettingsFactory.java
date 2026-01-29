package dev.zambone.account.testing;

import dev.zambone.account.domain.CreditCardSettings;
import dev.zambone.account.domain.CreditCardSettingsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CreditCardSettingsFactory {

    private final CreditCardSettingsRepository repository;

    public CreditCardSettingsFactory(CreditCardSettingsRepository repository) {
        this.repository = repository;
    }

    public CreditCardSettings createAndPersistCreditCardSettings(
            UUID accountId, int closingDay, int dueDay, BigDecimal limitAmount, UUID userId) {
        var creditCardSettings =
                new CreditCardSettings(
                        UUID.randomUUID(),
                        accountId,
                        closingDay,
                        dueDay,
                        limitAmount,
                        true,
                        Instant.now(),
                        Instant.now(),
                        null,
                        userId,
                        userId);
        return repository.save(creditCardSettings);
    }
}
