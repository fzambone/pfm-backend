plugins {
  id("java")
  id("com.google.protobuf") version "0.9.4"
}

group = "dev.zambone"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  // BCrypt for passwords
  implementation("org.mindrot:jbcrypt:0.4")

  // DATABASE (Postgres)
  implementation("org.postgresql:postgresql:42.7.2")

  // MIGRATIONS (Flyway)
  implementation("org.flywaydb:flyway-core:10.8.1")
  runtimeOnly("org.flywaydb:flyway-database-postgresql:10.8.1")

  // LOGGING (Production Grade)
  implementation("ch.qos.logback:logback-classic:1.5.3")
  implementation("net.logstash.logback:logstash-logback-encoder:7.4")

  // gRPC & Protobuf (The Communication Layer)
  implementation("io.grpc:grpc-netty-shaded:1.62.2")
  implementation("io.grpc:grpc-protobuf:1.62.2")
  implementation("io.grpc:grpc-stub:1.62.2")
  compileOnly("org.apache.tomcat:annotations-api:6.0.53")

  // TESTING
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.google.truth:truth:1.1.5")

  // TESTCONTAINERS
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks.test {
  useJUnitPlatform()
}

// CONFIGURATION FOR PROTOBUF PLUGIN
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.25.3"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.62.2"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        create("grpc")
      }
    }
  }
}
