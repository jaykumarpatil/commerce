plugins {
  `java-library`
}

dependencies {
  api(project(":shared"))
  implementation("org.springframework:spring-context")
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(26))
  }
}
