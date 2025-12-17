package dev.zambone.household.testing;

import dev.zambone.appusers.domain.AppUser;
import dev.zambone.household.domain.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class HouseholdFactory {

  private HouseholdFactory() {}

  public static Household createAndSaveHousehold(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository, String name, AppUser user) {

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
}
