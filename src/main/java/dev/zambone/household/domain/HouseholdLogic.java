package dev.zambone.household.domain;

import dev.zambone.appusers.domain.UserContext;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class HouseholdLogic {

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;

  public HouseholdLogic(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository) {
    this.householdRepository = householdRepository;
    this.householdMemberRepository = householdMemberRepository;
  }

  public Household create(UserContext userContext, String name) {
    var household = new Household(UUID.randomUUID(), name, true, Instant.now(), Instant.now(), userContext.actorId(), userContext.actorId());
    var householdMember = new HouseholdMember(household.id(), userContext.actorId(), Role.ADMIN, Instant.now(), userContext.actorId());

    var createdHousehold = householdRepository.save(household);
    householdMemberRepository.save(householdMember);
    return createdHousehold;
  }

  public Household getHousehold(UserContext userContext, UUID householdId) {
    return householdRepository.findByIdAndUserId(householdId, userContext.actorId())
        .orElseThrow(() -> new IllegalArgumentException("Household not found or access denied"));
  }

  public Household updateHousehold(UserContext userContext, UUID householdId, String newName, Instant version) {
    requireAdmin(householdId, userContext.actorId());
    Household existing = householdRepository.findByIdAndUserId(householdId, userContext.actorId())
        .orElseThrow(() -> new IllegalArgumentException("Household not found"));

    Household updatedHousehold = existing
        .withName(newName)
        .withUpdatedBy(userContext.actorId())
        .withUpdatedAt(Instant.now());

    boolean success = householdRepository.update(updatedHousehold, version);

    if (!success) {
      throw new ConcurrentModificationException("Data was modified by another user");
    }

    return updatedHousehold;
  }

  private void requireAdmin(UUID householdId, UUID userId) {
    var member = householdMemberRepository.findMembership(householdId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this household"));

    if (member.role() != Role.ADMIN) {
      throw new IllegalArgumentException("Only Admins can update household details");
    }
  }
}
