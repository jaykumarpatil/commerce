// Top-level Gradle configuration for a modular monolith Spring Boot app.
// Java language level will be driven by toolchains (Java 26 as requested).
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.toolchain.JavaLanguageVersion

allprojects {
  repositories {
    mavenCentral()
  }
}

subprojects {
  // Use Java toolchains to enforce Java 25 across Java modules
  plugins.withType<JavaPlugin> {
    extensions.configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
      }
    }
  }
}
