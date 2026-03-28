// Top-level Gradle configuration for a modular monolith Spring Boot app.
// Java language level will be driven by toolchains (Java 26 as requested).

allprojects {
  repositories {
    mavenCentral()
  }
}

subprojects {
  // Use Java toolchains to enforce Java 26 across modules
  java {
    toolchain {
      languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
    }
  }
}
