plugins {
  `java-library`
}

dependencies {
  api(project(":shared"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
  }
}
