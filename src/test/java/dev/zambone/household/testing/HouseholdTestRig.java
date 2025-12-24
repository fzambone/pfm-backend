package dev.zambone.household.testing;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.household.domain.Household;
import dev.zambone.household.domain.HouseholdLogic;

public class HouseholdTestRig {

  private final FakeHouseholdRepository householdRepository = new FakeHouseholdRepository();
  private final FakeHouseholdMemberRepository householdMemberRepository = new FakeHouseholdMemberRepository();
  private final HouseholdFactory factory = new HouseholdFactory(householdRepository, householdMemberRepository);
  private final HouseholdLogic logic;

  public HouseholdTestRig() {
    this.logic = new HouseholdLogic(householdRepository, householdMemberRepository);
  }

  public Household createPersistedHousehold(
      String name,
      AppUser user
  ) {
    return factory.createAndPersistHousehold(name, user);
  }

  public Household createDeletedHousehold(
      String name,
      AppUser user
  ) {
    return factory.createdDeletedHousehold(name, user);
  }

  public HouseholdLogic logic() {
    return logic;
  }

  public FakeHouseholdRepository repository() {
    return householdRepository;
  }

  public FakeHouseholdMemberRepository memberRepository() {
    return householdMemberRepository;
  }
}
