package dev.zambone.common.auth;

import dev.zambone.appusers.domain.UserContext;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AuthInterceptor implements ServerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next) {

    String userIdStr = headers.get(AuthConstants.USER_ID_HEADER);

    if (userIdStr == null || userIdStr.isBlank()) {
      call.close(Status.UNAUTHENTICATED.withDescription("Missing x-user-id header"), headers);
      return new ServerCall.Listener<>() {};
    }

    try {
      UUID userId = UUID.fromString(userIdStr);
      UserContext userContext = new UserContext(userId);

      Context ctx = Context.current().withValue(AuthConstants.USER_CONTEXT_KEY, userContext);

      return Contexts.interceptCall(ctx, call, headers, next);
    } catch (IllegalArgumentException e) {
      call.close(Status.UNAUTHENTICATED.withDescription("Invalid User ID format"), headers);
      return new ServerCall.Listener<>() {};
    }
  }
}
