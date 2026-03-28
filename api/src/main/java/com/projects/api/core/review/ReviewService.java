package com.projects.api.core.review;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

  @PostMapping(
    value = "/review",
    consumes = "application/json",
    produces = "application/json")
  Mono<Review> createReview(@RequestBody Review body);

  @GetMapping(
    value = "/review",
    produces = "application/json")
  Flux<Review> getReviews(
    @RequestHeader HttpHeaders headers,
    @RequestParam(value = "productId", required = true) int productId);

  @GetMapping(
    value = "/review/admin",
    produces = "application/json")
  Flux<Review> getReviewsForAdmin(@RequestParam(value = "productId", required = true) int productId);

  @PatchMapping(
    value = "/review/{reviewId}/moderation",
    produces = "application/json")
  Mono<Review> moderateReview(
    @PathVariable int reviewId,
    @RequestParam(value = "productId", required = true) int productId,
    @RequestParam(value = "status", required = true) ModerationStatus status);

  @GetMapping(
    value = "/review/summary",
    produces = "application/json")
  Mono<ReviewRatingSummary> getReviewSummary(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/review")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
