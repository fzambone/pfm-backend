package dev.zambone.household.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;
import dev.zambone.appuser.domain.UserContext;

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

  public Household create(UserContext context, String name) {
    var household = new Household(UUID.randomUUID(), name, true, Instant.now(), Instant.now(), null, context.actorId(), context.actorId());
    var householdMember = new HouseholdMember(household.id(), context.actorId(), Role.ADMIN, Instant.now(), null, context.actorId());

    var createdHousehold = householdRepository.save(household);
    householdMemberRepository.save(householdMember);
    return createdHousehold;
  }

  public Household get(UserContext context, UUID householdId) {
    return householdRepository.findByIdAndUserId(householdId, context.actorId())
        .orElseThrow(() -> new ResourceNotFoundException("Household not found or access denied"));
  }

  public Household update(UserContext context, UUID householdId, String newName, Instant version) {
    requireAdmin(householdId, context.actorId());
    var existingHousehold = householdRepository.findByIdAndUserId(householdId, context.actorId())
        .orElseThrow(() -> new ResourceNotFoundException("Household not found"));

    var updatedHousehold = existingHousehold
        .withName(newName)
        .withUpdatedBy(context.actorId())
        .withUpdatedAt(Instant.now());

    boolean success = householdRepository.update(updatedHousehold, version);

    if (!success) {
      throw new ConcurrentModificationException("Data was modified by another user");
    }

    return updatedHousehold;
  }

  public Household delete(UserContext context, UUID householdId, Instant version) {
    requireAdmin(householdId, context.actorId());
    var existingHousehold = householdRepository.findByIdAndUserId(householdId, context.actorId())
        .orElseThrow(() -> new IllegalArgumentException("Household not found"));

    Household deletedHousehold = existingHousehold
        .withDeletedAt(Instant.now())
        .withUpdatedAt(Instant.now());

    boolean success = householdRepository.delete(deletedHousehold, version);

    if (!success) throw new ConcurrentModificationException("Data was modified by another user");

    return deletedHousehold;
  }

  private void requireAdmin(UUID householdId, UUID userId) {
    var member = householdMemberRepository.findMembership(householdId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this household"));

    if (member.role() != Role.ADMIN) {
      throw new IllegalArgumentException("Only Admins can update household details");
    }
  }
}
