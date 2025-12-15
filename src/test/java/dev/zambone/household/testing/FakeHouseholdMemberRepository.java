package dev.zambone.household.testing;

import dev.zambone.household.domain.HouseholdMember;
import dev.zambone.household.domain.HouseholdMemberRepository;

import java.util.*;

public class FakeHouseholdMemberRepository implements HouseholdMemberRepository {

  private record CompositeKey(UUID householdId, UUID userId) {}

  private final Map<CompositeKey, HouseholdMember> table = new HashMap<>();

  @Override
  public HouseholdMember save(HouseholdMember householdMember) {
    var key = new CompositeKey(householdMember.householdId(), householdMember.appUserId());

    table.put(key, householdMember);

    return householdMember;
  }

  @Override
  public Optional<HouseholdMember> findByMemberId(UUID householdMemberId) {
    return table.values().stream()
        .filter(member -> member.appUserId().equals(householdMemberId))
        .findFirst();
  }

  @Override
  public Optional<HouseholdMember> findMembership(UUID householdId, UUID userId) {
    var key = new CompositeKey(householdId, userId);
    return Optional.ofNullable(table.get(key));
  }

}
