plugins {
  `java-library`
}

dependencies {
  api(project(":shared"))
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(25))
  }
}
