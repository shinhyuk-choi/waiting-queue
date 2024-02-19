package com.example.queuewebflux.service;

import com.example.queuewebflux.EmbeddedRedis;
import com.example.queuewebflux.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {

  @Autowired
  private UserQueueService userQueueService;

  @Autowired
  private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  @BeforeEach
  public void beforeEach() {
    ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory()
        .getReactiveConnection();

    reactiveConnection.serverCommands().flushAll().subscribe();
  }

  @Test
  void registerWaitQueue() {
    // given & when & then
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L))
        .expectNext(1L)
        .verifyComplete();

    StepVerifier.create(userQueueService.registerWaitQueue("test", 2L))
        .expectNext(2L)
        .verifyComplete();

    StepVerifier.create(userQueueService.registerWaitQueue("test", 3L))
        .expectNext(3L)
        .verifyComplete();
  }

  @Test
  void alreadyRegisterWaitQueue() {
    // given
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L))
        .expectNext(1L)
        .verifyComplete();

    // when & then
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L))
        .expectError(ApplicationException.class)
        .verify();

  }

  @Test
  public void emptyAllowUser() {
    // given & when & then
    StepVerifier.create(userQueueService.allowUser("test", 2L))
        .expectNext(0L)
        .verifyComplete();
  }


  @Test
  void allowUser() {
    // given
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L)
            .then(userQueueService.registerWaitQueue("test", 2L))
            .then(userQueueService.registerWaitQueue("test", 3L))
        ).expectNext(3L)
        .verifyComplete();

    // when & then
    StepVerifier.create(userQueueService.allowUser("test", 2L))
        .expectNext(2L)
        .verifyComplete();

    StepVerifier.create(userQueueService.allowUser("test", 2L))
        .expectNext(1L)
        .verifyComplete();

    StepVerifier.create(userQueueService.allowUser("test", 2L))
        .expectNext(0L)
        .verifyComplete();
  }

  @Test
  void registerWaitQueueAfterAllowUser() {
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L)
            .then(userQueueService.registerWaitQueue("test", 2L))
            .then(userQueueService.registerWaitQueue("test", 3L))
            .then(userQueueService.allowUser("test", 2L))
            .then(userQueueService.registerWaitQueue("test", 4L))
        ).expectNext(2L)
        .verifyComplete();
  }


  @Test
  void isNotAllowed() {
    StepVerifier.create(userQueueService.isAllowed("test", 1L)
        ).expectNext(false)
        .verifyComplete();
  }

  @Test
  void isAllowed() {
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L)
            .then(userQueueService.allowUser("test", 1L))
            .then(userQueueService.isAllowed("test", 1L))
        ).expectNext(true)
        .verifyComplete();
  }

  @Test
  void getRank() {
    StepVerifier.create(userQueueService.registerWaitQueue("test", 1L)
            .then(userQueueService.registerWaitQueue("test", 2L))
            .then(userQueueService.registerWaitQueue("test", 3L))
            .then(userQueueService.getRank("test", 2L))
        ).expectNext(2L)
        .verifyComplete();
  }

  @Test
  void emptyRank() {
    StepVerifier.create(userQueueService.getRank("test", 1L)
        ).expectNext(-1L)
        .verifyComplete();
  }
}