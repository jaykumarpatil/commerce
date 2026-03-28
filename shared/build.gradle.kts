plugins {
  `java-library`
}

java {
  toolchain {
    languageVersion.set(org.gradle.jvm.toolchains.JavaLanguageVersion.of(25))
  }
}
dependencies {
  testImplementation("com.tngtech.archunit:archunit-junit5:0.29.1")
}
