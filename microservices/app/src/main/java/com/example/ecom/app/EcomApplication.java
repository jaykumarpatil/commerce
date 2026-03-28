package com.projects.ecom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
  "com.projects.ecom.app",
  "com.projects.ecom.catalog",
  "com.projects.ecom.user",
  "com.projects.ecom.cart",
  "com.projects.ecom.order",
  "com.projects.ecom.payment",
  "com.projects.ecom.shared"
})
public class EcomApplication {
  public static void main(String[] args) {
    SpringApplication.run(EcomApplication.class, args);
  }
}
