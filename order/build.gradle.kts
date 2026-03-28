plugins {
  `java-library`
}

dependencies {
  api(project(":shared"))
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  runtimeOnly("org.postgresql:postgresql")
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
  }
}
