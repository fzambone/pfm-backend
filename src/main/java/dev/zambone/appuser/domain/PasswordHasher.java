package dev.zambone.appuser.domain;

public interface PasswordHasher {

  String hash(String rawPassword);
}
