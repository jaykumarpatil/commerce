plugins {
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.0"
  java
}

group = "com.project.webhook"
version = "0.1.0-SNAPSHOT"

dependencies {
  implementation(project(":shared"))
  implementation("org.springframework.boot:spring-boot-starter-web")
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
  }
}
