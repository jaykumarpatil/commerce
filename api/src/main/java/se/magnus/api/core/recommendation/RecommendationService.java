package se.magnus.api.core.recommendation;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

  @PostMapping(
    value = "/recommendation",
    consumes = "application/json",
    produces = "application/json")
  Mono<Recommendation> createRecommendation(@RequestBody Recommendation body);

  /**
   * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
   *
   * @param productId Id of the product
   * @return the recommendations of the product
   */
  @GetMapping(
    value = "/recommendation",
    produces = "application/json")
  Flux<Recommendation> getRecommendations(
    @RequestHeader HttpHeaders headers,
    @RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/recommendation")
  Mono<Void> deleteRecommendations(@RequestParam(value = "productId", required = true) int productId);
}
