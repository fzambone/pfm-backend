package dev.zambone.household.domain;

import dev.zambone.appusers.domain.UserContext;

import java.util.Optional;
import java.util.UUID;

public interface HouseholdRepository {

  Optional<Household> findByIdAndUserId(UUID householdId, UUID userId);

  Household save(Household household);
}
