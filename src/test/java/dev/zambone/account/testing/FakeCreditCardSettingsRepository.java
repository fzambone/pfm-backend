package dev.zambone.account.testing;

import dev.zambone.account.domain.CreditCardSettings;
import dev.zambone.account.domain.CreditCardSettingsRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class FakeCreditCardSettingsRepository implements CreditCardSettingsRepository {

  private final HashMap<UUID, CreditCardSettings> table = new HashMap<>();

  @Override
  public CreditCardSettings save(CreditCardSettings creditCardSettings) {
    table.put(creditCardSettings.id(), creditCardSettings);
    return table.get(creditCardSettings.id());
  }

  @Override
  public Optional<CreditCardSettings> findById(UUID creditCardSettingsId) {
    var ccSettings = table.get(creditCardSettingsId);
    if (ccSettings == null) {
      return Optional.empty();
    }
    return Optional.of(ccSettings);
  }

  @Override
  public Optional<CreditCardSettings> findByAccountId(UUID accountId) {
    return table.values().stream()
        .filter(ccSettings -> ccSettings.accountId().equals(accountId))
        .findAny();
  }

  @Override
  public boolean update(CreditCardSettings updatedCcSettings, Instant version) {

    var existingCcSettings = table.get(updatedCcSettings.id());

    if (existingCcSettings == null) {
      return false;
    }

    if (version != existingCcSettings.updatedAt()) {
      return false;
    }

    table.put(updatedCcSettings.id(), updatedCcSettings);
    return true;
  }
}
