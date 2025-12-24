package dev.zambone.household.testing;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.household.domain.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class HouseholdFactory {

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;

  public HouseholdFactory(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository) {
    this.householdRepository = householdRepository;
    this.householdMemberRepository = householdMemberRepository;
  }

  public Household createAndPersistHousehold(
      String name,
      AppUser user
  ) {

    var household = new Household(
        UUID.randomUUID(),
        name,
        true,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        user.id(),
        user.id()
    );

    var householdMember = new HouseholdMember(
        household.id(),
        user.id(),
        Role.ADMIN,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        user.id()
    );

    var savedHousehold = householdRepository.save(household);
    householdMemberRepository.save(householdMember);

    return savedHousehold;
  }

  public Household createdDeletedHousehold(
      String name,
      AppUser user) {

    var household = new Household(
        UUID.randomUUID(),
        name,
        true,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        user.id(),
        user.id()
    );

    var householdMember = new HouseholdMember(
        household.id(),
        user.id(),
        Role.ADMIN,
        Instant.now().truncatedTo(ChronoUnit.MICROS),
        null,
        user.id()
    );

    var deletedHousehold = householdRepository.save(household);
    householdMemberRepository.save(householdMember);

    return deletedHousehold;

  }
}
