plugins {
  id("org.springframework.boot") version "4.0.5"
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
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
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
