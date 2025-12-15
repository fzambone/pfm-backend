package dev.zambone.household.testing;

import dev.zambone.household.domain.Household;
import dev.zambone.household.domain.HouseholdRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeHouseholdRepository implements HouseholdRepository {

  private final Map<UUID, Household> table = new HashMap<>();


  @Override
  public Optional<Household> findByIdAndUserId(UUID householdId, UUID userId) {
    var household = table.get(householdId);

    if (household != null && household.createdBy().equals(userId)) {
      return Optional.of(household);
    }
    return Optional.empty();
  }

  @Override
  public Household save(Household household) {
    table.put(household.id(), household);
    return table.get(household.id());
  }
}
