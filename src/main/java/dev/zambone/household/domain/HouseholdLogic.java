package dev.zambone.household.domain;

import dev.zambone.appusers.domain.UserContext;

import java.time.Instant;
import java.util.UUID;

public class HouseholdLogic {

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;

  public HouseholdLogic(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository) {
    this.householdRepository = householdRepository;
    this.householdMemberRepository = householdMemberRepository;
  }

  public Household create(UserContext context, String name) {
    var household = new Household(UUID.randomUUID(), name, true, Instant.now(), Instant.now(), context.actorId(), context.actorId());
    var householdMember = new HouseholdMember(household.id(), context.actorId(), Role.ADMIN, Instant.now(), context.actorId());

    var createdHousehold = householdRepository.save(household);
    householdMemberRepository.save(householdMember);
    return createdHousehold;
  }

  public Household getHousehold(UserContext userContext, UUID householdId) {
    return householdRepository.findByIdAndUserId(householdId, userContext.actorId())
        .orElseThrow(() -> new IllegalArgumentException("Household not found or access denied"));
  }
}
