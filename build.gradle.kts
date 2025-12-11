plugins {
  id("java")
}

group = "dev.zambone"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  // GUICE (Dependency Injection - The Google Core)
  implementation("com.google.inject:guice:7.0.0")

  // BCRYPT for password hashing
  implementation("org.mindrot:jbcrypt:0.4")

  // DATABASE (JDBI + Postgres)
  implementation("org.jdbi:jdbi3-core:3.45.1")
  implementation("org.jdbi:jdbi3-postgres:3.45.1")
  implementation("org.postgresql:postgresql:42.7.2")

  // CONNECTION POOLING (HikariCP - The Industry Standard)
  implementation("com.zaxxer:HikariCP:5.1.0")

  // JUNIT 5 (Testing Standard)
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")

  // GOOGLE TRUTH (The "Google Style" Assertions)
  testImplementation("com.google.truth:truth:1.1.5")

}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21)) // Enforce Java 21 LTS
  }
}

tasks.test {
  useJUnitPlatform()
}
