package com.example.ecom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
  "com.example.ecom.app",
  "com.example.ecom.catalog",
  "com.example.ecom.user",
  "com.example.ecom.cart",
  "com.example.ecom.order",
  "com.example.ecom.payment",
  "com.example.ecom.shared"
})
public class EcomApplication {
  public static void main(String[] args) {
    SpringApplication.run(EcomApplication.class, args);
  }
}
