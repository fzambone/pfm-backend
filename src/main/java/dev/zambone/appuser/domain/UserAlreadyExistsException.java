package dev.zambone.appuser.domain;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("AppUser with email '" + email + "' already exists");
  }
}
