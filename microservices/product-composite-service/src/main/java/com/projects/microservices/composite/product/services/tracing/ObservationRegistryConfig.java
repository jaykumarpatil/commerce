package com.projects.microservices.composite.product.services.tracing;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ObservationRegistryConfig {

  private final BuildProperties buildProperties;

  public ObservationRegistryConfig(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Bean
  public ObservationRegistryCustomizer observationRegistryCustomizer() {
    return registry -> {
      registry.observationConfig().observationFilter(new BuildInfoObservationFilter(buildProperties));
    };
  }

  @FunctionalInterface
  public interface ObservationRegistryCustomizer<T extends ObservationRegistry> {
    void customize(T registry);
  }
}
