package dev.zambone.appuser.domain;

import dev.zambone.account.exceptions.ResourceNotFoundException;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class AppUserLogic {

  private final AppUserRepository appUserRepository;
  private final PasswordHasher passwordHasher;

  public AppUserLogic(AppUserRepository appUserRepository, PasswordHasher passwordHasher) {
    this.appUserRepository = appUserRepository;
    this.passwordHasher = passwordHasher;
  }

  public AppUser create(UserContext context, String email, String rawPassword, String fullName) {
    var existingUserOpt = appUserRepository.findByEmail(email);
    if(existingUserOpt.isPresent()) {
      var existingUser = existingUserOpt.get();

      if (existingUser.deletedAt() == null) {
        throw new UserAlreadyExistsException(email);
      } else {
        return reactivateUser(existingUser, rawPassword, fullName, context);
      }
    }

    var user = new AppUser(
        UUID.randomUUID(),
        email,
        passwordHasher.hash(rawPassword),
        fullName,
        Instant.now(),
        Instant.now(),
        null,
        context.actorId(),
        context.actorId());

    return appUserRepository.save(user);
  }

  public AppUser get(UserContext context, UUID userId) {

    if (!context.actorId().equals(userId)) {
      throw new ResourceNotFoundException("User not found");
    }

    return appUserRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  public AppUser update(UserContext context, UUID userId, String newName, Instant version) {
    var existingUser = appUserRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    var updatedUser = existingUser
        .withFullName(newName)
        .withUpdatedBy(context.actorId())
        .withUpdatedAt(Instant.now());

    boolean success = appUserRepository.update(updatedUser, version);

    if (!success) {
      throw new ConcurrentModificationException("Data was modified by another user. Try again");
    }

    return updatedUser;
  }

  private AppUser reactivateUser(AppUser existingUser, String newRawPassword, String newFullName, UserContext userContext) {
    Instant versionToMatch = existingUser.updatedAt();

    var reactivatedUser = existingUser
        .withDeletedAt(null)
        .withPasswordHash(passwordHasher.hash(newRawPassword))
        .withFullName(newFullName)
        .withUpdatedAt(Instant.now())
        .withUpdatedBy(userContext.actorId());

    var success = appUserRepository.update(reactivatedUser, versionToMatch);

    if(!success) {
      throw new ConcurrentModificationException("Data was modified by another user. Try again");
    }

    return reactivatedUser;
  }
}
