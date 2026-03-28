package com.projects.api.core.review;

import com.projects.api.core.common.PaginationRequest;
import com.projects.api.core.common.PaginationResponse;
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

  default Mono<PaginationResponse<Review>> getReviews(
    HttpHeaders headers,
    int productId,
    PaginationRequest paginationRequest) {

    final PaginationRequest request = paginationRequest == null ? new PaginationRequest() : paginationRequest;
    return getReviews(headers, productId)
      .skip((long) request.getPage() * request.getSize())
      .take(request.getSize())
      .collectList()
      .map(items -> PaginationResponse.of(items, request.getPage(), request.getSize()));
  }

  Mono<Review> updateReviewStatus(int productId, int reviewId, ReviewStatus status);

  @Deprecated
  @PatchMapping(value = "/review/status")
  default Mono<Review> updateReviewStatus(
    @RequestParam(value = "productId", required = true) int productId,
    @RequestParam(value = "reviewId", required = true) int reviewId,
    @RequestParam(value = "status", required = true) String status) {

    return updateReviewStatus(productId, reviewId, ReviewStatus.from(status));
  }

  @DeleteMapping(value = "/review")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
