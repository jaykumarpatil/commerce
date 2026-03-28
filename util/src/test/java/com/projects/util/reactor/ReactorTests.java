package com.projects.util.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ReactorTests {

  @Test
  void testFlux() {
    Flux<Integer> result = Flux.just(1, 2, 3, 4)
      .filter(n -> n % 2 == 0)
      .map(n -> n * 2);

    StepVerifier.create(result)
      .expectNext(4, 8)
      .verifyComplete();
  }

  @Test
  void testFluxBlocking() {

    List<Integer> list = Flux.just(1, 2, 3, 4)
      .filter(n -> n % 2 == 0)
      .map(n -> n * 2)
      .log()
      .collectList().block();

    assertThat(list).containsExactly(4, 8);
  }

  @Test
  void testFluxErrorPath() {
    Flux<Integer> result = Flux.just(1, 2, 3)
      .map(n -> {
        if (n == 2) throw new IllegalStateException("boom");
        return n;
      });

    StepVerifier.create(result)
      .expectNext(1)
      .expectErrorMessage("boom")
      .verify();
  }
}
