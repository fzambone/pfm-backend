package dev.zambone.appuser.domain;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record UserContext(UUID actorId, Set<UUID> accessibleHouseholdIds) {

  public UserContext(UUID actorId) {
    this(actorId, Collections.emptySet());
  }

  public boolean hasAccessToHousehold(UUID householdId) {
    return accessibleHouseholdIds.contains(householdId);
  }
}
