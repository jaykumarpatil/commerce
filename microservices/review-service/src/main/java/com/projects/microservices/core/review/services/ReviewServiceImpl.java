package com.projects.microservices.core.review.services;

import static java.util.logging.Level.FINE;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import com.projects.api.core.review.ModerationStatus;
import com.projects.api.core.review.Review;
import com.projects.api.core.review.ReviewRatingSummary;
import com.projects.api.core.review.ReviewService;
import com.projects.api.core.review.ReviewStatus;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.review.persistence.ReviewEntity;
import com.projects.microservices.core.review.persistence.ReviewRepository;
import com.projects.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ReviewRepository repository;
  private final ReviewMapper mapper;
  private final ServiceUtil serviceUtil;
  private final Scheduler jdbcScheduler;
  private final WebClient webClient;
  private final String userServiceUrl;
  private final String productServiceUrl;

  @Autowired
  public ReviewServiceImpl(
      @Qualifier("jdbcScheduler") Scheduler jdbcScheduler,
      ReviewRepository repository,
      ReviewMapper mapper,
      ServiceUtil serviceUtil,
      WebClient.Builder webClientBuilder,
      @Value("${app.userServiceUrl:http://user-service}") String userServiceUrl,
      @Value("${app.productServiceUrl:http://product}") String productServiceUrl) {
    this.jdbcScheduler = jdbcScheduler;
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
    this.webClient = webClientBuilder.build();
    this.userServiceUrl = userServiceUrl;
    this.productServiceUrl = productServiceUrl;
  }

  @Override
  public Mono<Review> createReview(Review body) {
    validateProductId(body.getProductId());
    validateUserId(body.getUserId());
    validateRating(body.getRating());

    return ensureUserExists(body.getUserId())
      .then(ensureProductExists(body.getProductId()))
      .then(Mono.fromCallable(() -> internalCreateReview(body)))
      .subscribeOn(jdbcScheduler);
  }

  private Review internalCreateReview(Review body) {
    try {
      ReviewEntity entity = mapper.apiToEntity(body);
      if (entity.getModerationStatus() == null) {
        entity.setModerationStatus(ModerationStatus.PENDING);
      }
      if (entity.getCreatedAt() == null) {
        entity.setCreatedAt(Instant.now());
      }
      entity.setUpdatedAt(Instant.now());
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("createReview: created a review entity: {}/{}/{}", body.getProductId(), body.getReviewId(), body.getUserId());
      return mapper.entityToApi(newEntity);

    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", User Id:" + body.getUserId());
    }
  }

  @Override
  public Flux<Review> getReviews(HttpHeaders headers, int productId) {
    validateProductId(productId);
    LOG.info("Will get APPROVED reviews for product with id={}", productId);

    return Mono.fromCallable(() -> internalGetReviewsByStatus(productId, ModerationStatus.APPROVED))
      .flatMapMany(Flux::fromIterable)
      .log(LOG.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  @Override
  public Flux<Review> getReviewsForAdmin(int productId) {
    validateProductId(productId);
    LOG.info("Will get all reviews for admin, product id={}", productId);

    return Mono.fromCallable(() -> internalGetAllReviews(productId))
      .flatMapMany(Flux::fromIterable)
      .log(LOG.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  @Override
  public Mono<Review> moderateReview(int reviewId, int productId, ModerationStatus status) {
    validateProductId(productId);
    if (reviewId < 1) {
      throw new InvalidInputException("Invalid reviewId: " + reviewId);
    }
    if (status == null) {
      throw new InvalidInputException("Moderation status is required");
    }

    return Mono.fromCallable(() -> internalModerateReview(reviewId, productId, status))
      .subscribeOn(jdbcScheduler);
  }

  private Review internalModerateReview(int reviewId, int productId, ModerationStatus status) {
    ReviewEntity entity = repository.findByProductIdAndReviewId(productId, reviewId)
      .orElseThrow(() -> new NotFoundException("No review found for productId: " + productId + ", reviewId: " + reviewId));

    entity.setModerationStatus(status);
    entity.setUpdatedAt(Instant.now());
    ReviewEntity saved = repository.save(entity);

    Review review = mapper.entityToApi(saved);
    review.setServiceAddress(serviceUtil.getServiceAddress());
    return review;
  }

  @Override
  public Mono<ReviewRatingSummary> getReviewSummary(int productId) {
    validateProductId(productId);

    return Mono.fromCallable(() -> internalGetReviewSummary(productId))
      .subscribeOn(jdbcScheduler);
  }

  private ReviewRatingSummary internalGetReviewSummary(int productId) {
    List<ReviewEntity> approvedReviews = repository.findByProductIdAndModerationStatus(productId, ModerationStatus.APPROVED);

    Map<Integer, Long> countByRating = approvedReviews.stream()
      .collect(Collectors.groupingBy(ReviewEntity::getRating, Collectors.counting()));

    double averageRating = approvedReviews.stream()
      .mapToInt(ReviewEntity::getRating)
      .average()
      .orElse(0.0);

    return new ReviewRatingSummary(productId, averageRating, approvedReviews.size(), countByRating);
  }

  private List<Review> internalGetReviewsByStatus(int productId, ModerationStatus status) {
    List<ReviewEntity> entityList = repository.findByProductIdAndModerationStatus(productId, status);
    return enrichWithServiceAddress(entityList);
  }

  private List<Review> internalGetAllReviews(int productId) {
    List<ReviewEntity> entityList = repository.findByProductId(productId);
    return enrichWithServiceAddress(entityList);
  }

  private List<Review> enrichWithServiceAddress(List<ReviewEntity> entityList) {
    List<Review> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    LOG.debug("Response size: {}", list.size());
    return list;
  }


  @Override
  public Mono<Review> updateReviewStatus(int productId, int reviewId, ReviewStatus status) {
    validateProductId(productId);
    if (reviewId < 1) {
      throw new InvalidInputException("Invalid reviewId: " + reviewId);
    }
    if (status == null) {
      throw new InvalidInputException("Review status is required");
    }

    return Mono.fromCallable(() -> {
      ReviewEntity entity = repository.findByProductIdAndReviewId(productId, reviewId)
        .orElseThrow(() -> new NotFoundException("No review found for productId: " + productId + ", reviewId: " + reviewId));

      entity.setStatus(status);
      if (ReviewStatus.APPROVED.equals(status)) {
        entity.setModerationStatus(ModerationStatus.APPROVED);
      } else if (ReviewStatus.REJECTED.equals(status)) {
        entity.setModerationStatus(ModerationStatus.REJECTED);
      }
      entity.setUpdatedAt(Instant.now());

      ReviewEntity saved = repository.save(entity);
      Review review = mapper.entityToApi(saved);
      review.setServiceAddress(serviceUtil.getServiceAddress());
      return review;
    }).subscribeOn(jdbcScheduler);
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    validateProductId(productId);
    return Mono.fromRunnable(() -> internalDeleteReviews(productId)).subscribeOn(jdbcScheduler).then();
  }

  private void internalDeleteReviews(int productId) {
    LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }

  private void validateProductId(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }
  }

  private void validateRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw new InvalidInputException("Invalid rating, expected range 1..5 but was: " + rating);
    }
  }

  private void validateUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new InvalidInputException("Invalid userId: " + userId);
    }
  }

  private Mono<Void> ensureUserExists(String userId) {
    return webClient.get()
      .uri(userServiceUrl + "/v1/users/{userId}", userId)
      .retrieve()
      .toBodilessEntity()
      .then()
      .onErrorMap(WebClientResponseException.NotFound.class,
        ex -> new NotFoundException("User not found: " + userId));
  }

  private Mono<Void> ensureProductExists(int productId) {
    return webClient.get()
      .uri(productServiceUrl + "/product/{productId}", productId)
      .retrieve()
      .toBodilessEntity()
      .then()
      .onErrorMap(WebClientResponseException.NotFound.class,
        ex -> new NotFoundException("Product not found: " + productId));
  }
}
