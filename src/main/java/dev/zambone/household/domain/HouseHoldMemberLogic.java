package dev.zambone.household.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class HouseHoldMemberLogic {

  private final HouseholdMemberRepository householdMemberRepository;

  public HouseHoldMemberLogic(HouseholdMemberRepository householdMemberRepository) {
    this.householdMemberRepository = householdMemberRepository;
  }

  public HouseholdMember createHouseholdMember(UUID householdId, UUID appUserId, Role role, UUID invitedBy) {
    var householdMember = new HouseholdMember(householdId, appUserId, role, Instant.now(), null, invitedBy);
    return householdMemberRepository.save(householdMember);
  }

  public Optional<HouseholdMember> findHouseholdMemberByHouseholdId(UUID householdId) {
    return householdMemberRepository.findByMemberId(householdId);
  }
}
