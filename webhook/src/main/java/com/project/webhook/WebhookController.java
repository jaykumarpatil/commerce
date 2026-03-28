package com.project.webhook;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
  private final ApplicationEventPublisher publisher;

  public WebhookController(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @PostMapping("/{provider}")
  public ResponseEntity<String> onWebhook(
      @PathVariable String provider,
      @RequestHeader(value = "X-Signature", required = false) String signature,
      @RequestBody(required = false) Map<String, Object> payload
  ) {
    if (!StringUtils.hasText(provider)) {
      throw new ResponseStatusException(BAD_REQUEST, "Missing provider");
    }

    if (!StringUtils.hasText(signature)) {
      throw new ResponseStatusException(BAD_REQUEST, "Missing signature");
    }

    if (payload == null || payload.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, "Missing payload");
    }

    String normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
    publisher.publishEvent(new WebhookEvent(normalizedProvider, Map.copyOf(payload)));

    return ResponseEntity.ok("ok");
  }
}

record WebhookEvent(String provider, Map<String, Object> payload) {
}
