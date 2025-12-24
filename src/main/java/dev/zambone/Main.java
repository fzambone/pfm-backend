package dev.zambone;

import dev.zambone.appuser.domain.AppUser;
import dev.zambone.appuser.storage.SqlAppUserRepository;
import dev.zambone.common.auth.AuthInterceptor;
import dev.zambone.household.domain.HouseholdLogic;
import dev.zambone.household.service.HouseholdGrpcService;
import dev.zambone.household.storage.SqlHouseholdMemberRepository;
import dev.zambone.household.storage.SqlHouseholdRepository;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static final UUID DEV_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  public static void main(String[] args) throws IOException, InterruptedException {
    logger.info("Starting FPM Backend...");

    // 1. Infrastructure (Database)
    DataSource dataSource = createDataSource();
    runMigrations(dataSource);

    // 2. Composition Root (Manual Dependency Injection)
    var householdRepository = new SqlHouseholdRepository(dataSource);
    var memberRepository = new SqlHouseholdMemberRepository(dataSource);
    var appUserRepository = new SqlAppUserRepository(dataSource);

    bootstrapDevUser(appUserRepository);

    var householdLogic = new HouseholdLogic(householdRepository, memberRepository);
    var householdGrpcService = new HouseholdGrpcService(householdLogic);

    // 3. Server Configuration (Java 21 Virtual Threads)
    int port = 50051;
    Server server = ServerBuilder.forPort(port)
        .executor(Executors.newVirtualThreadPerTaskExecutor())
        .addService(ServerInterceptors.intercept(householdGrpcService, new AuthInterceptor()))
        .build()
        .start();

    logger.info("Server started, listening on port {}", port);

    // 4. The Shutdown Hook (Graceful Termination)
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Received shutdown request. Stopping server...");
      // Stop accepting calls
      server.shutdown();
      try {
        // Wait 30s for existing calls to finish
        if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
          server.shutdownNow();
          logger.warn("Server forced to shutdown after timeout.");
        }
      } catch (InterruptedException e) {
        server.shutdownNow();
      }
      logger.info("Server stopped.");
    }));

    // 5. Keep the main thread alive
    server.awaitTermination();
  }

  private static void runMigrations(DataSource dataSource) {
    logger.info("Running Database Migrations...");
    Flyway.configure()
        .dataSource(dataSource)
        .load()
        .migrate();
    logger.info("Migrations applied successfully.");
  }

  private static DataSource createDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();

    dataSource.setServerNames(new String[]{System.getenv().getOrDefault("DB_HOST", "localhost")});
    dataSource.setPortNumbers(new int[]{Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"))});
    dataSource.setDatabaseName(System.getenv().getOrDefault("DB_NAME", "pfm_dev"));
    dataSource.setUser(System.getenv().getOrDefault("DB_USER", "pfm_user"));
    dataSource.setPassword(System.getenv().getOrDefault("DB_PASS", "secret_password"));

    return dataSource;
  }

  private static void bootstrapDevUser(SqlAppUserRepository appUserRepository) {
    if (appUserRepository.findByEmail("dev@zambone.dev").isEmpty()) {
      logger.info("Bootstrapping Dev User (ID: {}...", DEV_USER_ID);

      // Keeping password plain for dev purposes only
      AppUser devUser = new AppUser(
          DEV_USER_ID,
          "dev@zambone.dev",
          "hashed_secret",
          "Dev Administrator",
          Instant.now(),
          Instant.now(),
          null,
          DEV_USER_ID,
          DEV_USER_ID
      );
      appUserRepository.save(devUser);
    }
  }
}
