package dev.zambone.common.auth;

import dev.zambone.appusers.domain.UserContext;
import io.grpc.Context;
import io.grpc.Metadata;

public class AuthConstants {
  // The expected header key for now is 'x-user-id: <UUID>'
  public static final Metadata.Key<String> USER_ID_HEADER =
      Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

  public static final Context.Key<UserContext> USER_CONTEXT_KEY =
      Context.key("user-context");
}
