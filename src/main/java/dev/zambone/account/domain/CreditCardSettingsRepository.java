package dev.zambone.account.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardSettingsRepository {

  CreditCardSettings save(CreditCardSettings creditCardSettings);

  Optional<CreditCardSettings> findById(UUID creditCardSettingsId);

  Optional<CreditCardSettings> findByAccountId(UUID accountId);

  boolean update(CreditCardSettings updatedCcSettings, Instant version);
}
