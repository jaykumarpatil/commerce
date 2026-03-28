package com.projects.microservices.core.recommendation.services;

import static java.util.logging.Level.FINE;

import com.projects.api.core.recommendation.Recommendation;
import com.projects.api.core.recommendation.RecommendationService;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.microservices.core.recommendation.persistence.RecommendationEntity;
import com.projects.microservices.core.recommendation.persistence.RecommendationRepository;
import com.projects.util.http.ServiceUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
  private static final String USER_ID_HEADER = "X-User-Id";

  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;
  private final ServiceUtil serviceUtil;
  private final RecommendationCache cache;
  private final WebClient webClient;
  private final String analyticsServiceUrl;

  @Autowired
  public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil,
      RecommendationCache cache, WebClient.Builder webClientBuilder,
      @Value("${app.analyticsServiceUrl:http://analytics-service}") String analyticsServiceUrl) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
    this.cache = cache;
    this.webClient = webClientBuilder.build();
    this.analyticsServiceUrl = analyticsServiceUrl;
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
    if (body.getRate() < 1 || body.getRate() > 5) throw new InvalidInputException("Invalid rate, expected 1..5 but was: " + body.getRate());

    RecommendationEntity entity = mapper.apiToEntity(body);
    return repository.save(entity)
      .doOnSuccess(saved -> cache.evictByProductId(saved.getProductId()))
      .log(LOG.getName(), FINE)
      .onErrorMap(DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
      .map(mapper::entityToApi)
      .map(this::setServiceAddress);
  }

  @Override
  public Flux<Recommendation> getRecommendations(HttpHeaders headers, int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    String userId = headers.getFirst(USER_ID_HEADER);
    String cacheKey = productId + "::" + (userId == null ? "anon" : userId);
    List<Recommendation> cached = cache.get(cacheKey);
    if (cached != null) {
      return Flux.fromIterable(cached);
    }

    LOG.info("Will get personalized recommendations for product id={}, userId={}", productId, userId);
    return repository.findByProductId(productId)
      .collectList()
      .flatMapMany(raw -> enrichAndScore(raw, productId, userId))
      .map(this::setServiceAddress)
      .collectList()
      .doOnNext(list -> cache.put(cacheKey, list))
      .flatMapMany(Flux::fromIterable);
  }

  private Flux<Recommendation> enrichAndScore(List<RecommendationEntity> raw, int productId, String userId) {
    return fetchUserAffinity(userId, productId)
      .flatMapMany(affinity -> Flux.fromIterable(raw)
        .map(mapper::entityToApi)
        .map(rec -> applyPersonalizationScore(rec, affinity))
        .sort(Comparator.comparing(Recommendation::getPersonalizationScore, Comparator.nullsLast(Comparator.reverseOrder()))));
  }

  private Mono<Double> fetchUserAffinity(String userId, int productId) {
    if (userId == null || userId.isBlank()) {
      return Mono.just(0.0);
    }

    return webClient.get()
      .uri(analyticsServiceUrl + "/v1/analytics/recommendations/affinity?userId={userId}&productId={productId}", userId, productId)
      .retrieve()
      .bodyToMono(Double.class)
      .timeout(Duration.ofMillis(300))
      .onErrorResume(ex -> {
        LOG.debug("Analytics affinity lookup failed, fallback to neutral affinity. reason={}", ex.getMessage());
        return Mono.just(0.0);
      });
  }

  private Recommendation applyPersonalizationScore(Recommendation recommendation, double affinity) {
    double collaborativeWeight = recommendation.getRate() / 5.0;
    double historyWeight = recommendation.getUserId() != null && recommendation.getUserId().equalsIgnoreCase(recommendation.getAuthor()) ? 0.2 : 0.0;
    double finalScore = collaborativeWeight * 0.7 + affinity * 0.25 + historyWeight * 0.05;

    recommendation.setPersonalizationScore(finalScore);
    recommendation.setScoreReason(String.format("collaborative=%.2f,history=%.2f,analytics=%.2f", collaborativeWeight, historyWeight, affinity));
    recommendation.setGeneratedAt(Instant.now());
    return recommendation;
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
    LOG.debug("deleteRecommendations for productId={}", productId);
    cache.evictByProductId(productId);
    return repository.deleteAll(repository.findByProductId(productId));
  }

  private Recommendation setServiceAddress(Recommendation e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }

  @Component
  static class RecommendationCache {
    private static final Duration TTL = Duration.ofMinutes(5);
    private final Map<String, CacheEntry> entries = new ConcurrentHashMap<>();

    List<Recommendation> get(String key) {
      CacheEntry entry = entries.get(key);
      if (entry == null || entry.expiresAt.isBefore(Instant.now())) {
        entries.remove(key);
        return null;
      }
      return entry.value;
    }

    void put(String key, List<Recommendation> value) {
      entries.put(key, new CacheEntry(value, Instant.now().plus(TTL)));
    }

    void evictByProductId(int productId) {
      String prefix = productId + "::";
      entries.keySet().removeIf(k -> k.startsWith(prefix));
    }

    private record CacheEntry(List<Recommendation> value, Instant expiresAt) {}
  }
}
