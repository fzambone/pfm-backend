package dev.zambone.household.domain;

import java.util.Optional;
import java.util.UUID;

public interface HouseholdMemberRepository {

  HouseholdMember save(HouseholdMember householdMember);

  Optional<HouseholdMember> findByMemberId(UUID householdMemberId);

  Optional<HouseholdMember> findMembership(UUID householdId, UUID userId);
}
