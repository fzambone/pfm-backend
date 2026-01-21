import com.google.protobuf.gradle.id

plugins {
    id("java")
    id("com.google.protobuf") version "0.9.6"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.github.ben-manes.versions") version "0.53.0"
}

// Version constants
val grpcVersion = "1.78.0"
val flywayVersion = "11.20.2"
val protobufVersion = "4.33.4"
val testcontainersVersion = "1.21.4"
val junitVersion = "6.0.2"
val jbcryptVersion = "0.4"
val postgresqlVersion = "42.7.9"
val logbackVersion = "1.5.25"
val logstashVersion = "9.0"
val truthVersion = "1.4.5"
val annotationsApiVersion = "6.0.53"

group = "dev.zambone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("com.google.truth:truth:$truthVersion")
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    compileOnly("org.apache.tomcat:annotations-api:$annotationsApiVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.test {
    useJUnitPlatform()
}

// CONFIGURATION FOR PROTOBUF PLUGIN
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
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

// CONFIGURATION FOR SPOTLESS (Code Formatting)
spotless {
    java {
        target("src/*/java/**/*.java")
        googleJavaFormat("1.19.2").aosp().reflowLongStrings()
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}