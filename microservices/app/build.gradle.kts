plugins {
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.0"
  java
}

group = "com.projects.ecom"
version = "0.1.0-SNAPSHOT"

dependencies {
  implementation(project(":shared"))
  implementation(project(":catalog"))
  implementation(project(":cart"))
  implementation(project(":user"))
  implementation(project(":order"))
  implementation(project(":payment"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  runtimeOnly("org.postgresql:postgresql")
  implementation("com.vladmihalcea:hibernate-types-52") // JSONB support
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
  }
}

// Enable executable jar
tasks.named("bootJar") {
  archiveFileName.set("ecom-modular-monolith.jar")
}
