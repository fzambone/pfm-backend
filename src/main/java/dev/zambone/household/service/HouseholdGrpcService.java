package dev.zambone.household.service;

import dev.zambone.appusers.domain.UserContext;
import dev.zambone.common.auth.AuthConstants;
import dev.zambone.household.domain.HouseholdLogic;
import dev.zambone.proto.household.v1.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HouseholdGrpcService extends HouseholdServiceGrpc.HouseholdServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(HouseholdServiceGrpc.class);
  private final HouseholdLogic householdLogic;

  public HouseholdGrpcService(HouseholdLogic householdLogic) {
    this.householdLogic = householdLogic;
  }

  @Override
  public void createHousehold(
      CreateHouseholdRequest request,
      StreamObserver<CreateHouseholdResponse> responseStreamObserver) {

    try {
      // TODO: Need to implement real Auth2 mechanism, for now we are trusting a x-user-id header"
      // Getting user from context
      UserContext context = AuthConstants.USER_CONTEXT_KEY.get();

      if (context == null) {
        throw new IllegalArgumentException("No user context found!");
      }

      var domainHousehold = householdLogic.create(context, request.getName());
      Household protoHousehold = Household.newBuilder()
          .setId(domainHousehold.id().toString())
          .setName(domainHousehold.name())
          .build();

      CreateHouseholdResponse response = CreateHouseholdResponse.newBuilder()
          .setHousehold(protoHousehold)
          .build();

      responseStreamObserver.onNext(response);
      responseStreamObserver.onCompleted();

    } catch (Exception e) {
      logger.error("Failed to create Household", e);
       responseStreamObserver.onError(
           Status.INTERNAL
               .withDescription("An internal error occurred")
               .withCause(e)
               .asRuntimeException());
    }
  }

  @Override
  public void getHousehold(
      GetHouseholdRequest request,
      StreamObserver<GetHouseholdResponse> responseStreamObserver) {

    try {
      UserContext context = AuthConstants.USER_CONTEXT_KEY.get();

      if (context == null) {
        throw new IllegalArgumentException("No user context found!");
      }

      var domainHousehold = householdLogic.getHousehold(context, UUID.fromString(request.getHouseholdId()));
      Household protoHousehold = Household.newBuilder()
          .setId(domainHousehold.id().toString())
          .setName(domainHousehold.name())
          .build();

      GetHouseholdResponse response = GetHouseholdResponse.newBuilder()
          .setHousehold(protoHousehold)
          .build();

      responseStreamObserver.onNext(response);
      responseStreamObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      logger.warn("Household lookup failed: {}", e.getMessage());
      responseStreamObserver.onError(
          Status.NOT_FOUND
              .withDescription("Household not found or access denied")
              .asRuntimeException());
    } catch (Exception e) {
      logger.error("System failure in getHousehold", e);
      responseStreamObserver.onError(
          Status.INTERNAL
              .withDescription("Internal system error")
              .asRuntimeException());
    }
  }
}
