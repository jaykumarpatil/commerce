package com.project.webhook;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
  private final ApplicationEventPublisher publisher;
  public WebhookController(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @PostMapping("/{provider}")
  public String onWebhook(@PathVariable String provider, @RequestHeader("X-Signature") String signature, @RequestBody Map<String, Object> payload) {
    // Basic signature guard (placeholder)
    if (signature == null || signature.isEmpty()) {
      throw new IllegalArgumentException("Missing signature");
    }
    // Publish a generic webhook event inside the app for downstream handling
    publisher.publishEvent(new WebhookEvent(provider, payload));
    return "ok";
  }
}

class WebhookEvent {
  final String provider;
  final Map<String, Object> payload;
  WebhookEvent(String provider, Map<String, Object> payload) {
    this.provider = provider;
    this.payload = payload;
  }
}
