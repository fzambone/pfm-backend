package dev.zambone.household.service;

import dev.zambone.appusers.domain.UserContext;
import dev.zambone.common.auth.AuthConstants;
import dev.zambone.household.domain.HouseholdLogic;
import dev.zambone.proto.household.v1.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ConcurrentModificationException;
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
      var protoHousehold = convertToProto(domainHousehold);

      CreateHouseholdResponse response = CreateHouseholdResponse.newBuilder()
          .setHousehold(protoHousehold)
          .build();

      responseStreamObserver.onNext(response);
      responseStreamObserver.onCompleted();

    } catch (IllegalArgumentException e) {
      logger.error("Failed to create Household", e);
      responseStreamObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription(e.getMessage())
              .asRuntimeException());

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

      var domainHousehold = householdLogic.get(context, UUID.fromString(request.getHouseholdId()));
      var protoHousehold = convertToProto(domainHousehold);

      GetHouseholdResponse response = GetHouseholdResponse.newBuilder()
          .setHousehold(protoHousehold)
          .build();

      responseStreamObserver.onNext(response);
      responseStreamObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      logger.error("Household lookup failed: {}", e.getMessage());
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

  @Override
  public void updateHousehold(
      UpdateHouseholdRequest request,
      StreamObserver<UpdateHouseholdResponse> responseStreamObserver) {

    try {
      UserContext context = AuthConstants.USER_CONTEXT_KEY.get();
      if (context == null) throw new IllegalArgumentException("No user context");

      UUID id = UUID.fromString(request.getHouseholdId());

      Instant version = Instant.ofEpochSecond(
          request.getVersion().getSeconds(),
          request.getVersion().getNanos()
      );

      var updatedHousehold = householdLogic.update(
          context,
          id,
          request.getName(),
          version
      );
      var protoHousehold = convertToProto(updatedHousehold);

      UpdateHouseholdResponse response = UpdateHouseholdResponse.newBuilder()
          .setHousehold(protoHousehold)
          .build();

      responseStreamObserver.onNext(response);
      responseStreamObserver.onCompleted();

    } catch (ConcurrentModificationException e) {
      // CRITICAL CONCURRENCY MAPPING
      logger.error("Failed to update Household, data has changed during update: {}", e.getMessage());
      responseStreamObserver.onError(Status.ABORTED
          .withDescription("Data has changed. Please refresh and try again.")
          .asRuntimeException());
    } catch (IllegalArgumentException e) {
      // Handle Validation/Auth errors -> INVALID_ARGUMENT or NOT_FOUND
      logger.error("Failed to update Household, Validation/Auth error: {}", e.getMessage());
      responseStreamObserver.onError(Status.INVALID_ARGUMENT
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (Exception e) {
      logger.error("Internal failure updating Household: {}", e.getMessage());
      responseStreamObserver.onError(Status.INTERNAL
          .withDescription("Internal error")
          .asRuntimeException());
    }
  }

  Household convertToProto(dev.zambone.household.domain.Household domain) {
    var builder = Household.newBuilder()
        .setId(domain.id().toString())
        .setName(domain.name())
        .setIsActive(domain.isActive());

    if (domain.createdAt() != null) {
      builder.setCreatedAt(toProtoTimestamp(domain.createdAt()));
    }
    if (domain.updatedAt() != null) {
      builder.setUpdatedAt(toProtoTimestamp(domain.updatedAt()));
    }

    return builder.build();

  }

  private com.google.protobuf.Timestamp toProtoTimestamp(java.time.Instant instant) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
}
