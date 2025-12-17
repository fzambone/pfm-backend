package dev.zambone.household.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdRepository {

  Optional<Household> findByIdAndUserId(UUID householdId, UUID userId);

  Household save(Household household);

  boolean update(Household household, Instant version);

  boolean delete(Household household, Instant version);
}
