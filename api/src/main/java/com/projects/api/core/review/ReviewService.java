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

  /**
   * Sample usage: "curl $HOST:$PORT/review?productId=1".
   *
   * @param productId Id of the product
   * @return the reviews of the product
   */
  @GetMapping(
    value = "/review",
    produces = "application/json")
  Flux<Review> getReviews(
    @RequestHeader HttpHeaders headers,
    @RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/review")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
