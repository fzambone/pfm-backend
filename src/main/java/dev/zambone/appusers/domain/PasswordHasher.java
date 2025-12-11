package dev.zambone.appusers.domain;

public interface PasswordHasher {

  String hash(String rawPassword);
}
