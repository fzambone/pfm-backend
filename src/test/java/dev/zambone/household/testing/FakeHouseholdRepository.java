package dev.zambone.household.testing;

import dev.zambone.household.domain.Household;
import dev.zambone.household.domain.HouseholdRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeHouseholdRepository implements HouseholdRepository {

  private final Map<UUID, Household> table = new HashMap<>();


  @Override
  public Optional<Household> findByIdAndUserId(UUID householdId, UUID userId) {
    var household = table.get(householdId);
    if (household != null && household.createdBy().equals(userId) && !household.isDeleted()) {
      return Optional.of(household);
    }
    return Optional.empty();
  }

  @Override
  public Household save(Household household) {
    table.put(household.id(), household);
    return table.get(household.id());
  }

  @Override
  public boolean update(Household household, Instant version) {
    var existingHousehold = table.get(household.id());

    if (existingHousehold == null) return false;

    if (!existingHousehold.updatedAt().equals(version)) {
      return false;

    }

    table.put(household.id(), household);
    return true;
  }

  @Override
  public boolean delete(Household household, Instant version) {
    Household existing = table.get(household.id());

    if (existing == null) return false;

    if (!existing.updatedAt().equals(version)) {
      return false;
    }

    table.put(household.id(), household);
    return true;
  }
}
