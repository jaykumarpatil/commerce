plugins {
  `java-library`
}

group = "com.project.promotion"
version = "0.1.0-SNAPSHOT"

dependencies {
  api(project(":shared"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(25))
  }
}
