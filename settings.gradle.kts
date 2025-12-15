pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.google.protobuf") {
        useModule("com.google.protobuf:protobuf-gradle-plugin:${requested.version}")
      }
    }
  }
}

rootProject.name = "personal-finance-manager"
