package com.example.queuewebflux.service;

import com.example.queuewebflux.exception.ErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private final String USER_QUEUE_WAIT_KEY = "user:queue:%s:wait";
  private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "user:queue:*:wait";
  private final String USER_QUEUE_PROCEED_KEY = "user:queue:%s:proceed";

  @Value("${scheduler.enabled}")
  private Boolean scheduling = false;


  public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
    var unixTimestamp = Instant.now().getEpochSecond();
    return reactiveRedisTemplate.opsForZSet()
        .add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
        .filter(i -> i)
        .switchIfEmpty(Mono.error(ErrorCode.ALREADY_REGISTERED.build()))
        .flatMap(i -> reactiveRedisTemplate.opsForZSet()
            .rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
        .map(rank -> rank >= 0 ? rank + 1 : rank);
  }

  public Mono<Long> allowUser(final String queue, final Long count) {
    return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
        .flatMap(result -> reactiveRedisTemplate.opsForZSet()
            .add(USER_QUEUE_PROCEED_KEY.formatted(queue), result.getValue(),
                Instant.now().getEpochSecond()))
        .count();

  }

  public Mono<Boolean> isAllowed(final String queue, final Long userId) {
    return reactiveRedisTemplate.opsForZSet()
        .rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
        .defaultIfEmpty(-1L)
        .map(rank -> rank >= 0);
  }

  public Mono<Long> getRank(final String queue, final Long userId) {
    return reactiveRedisTemplate.opsForZSet()
        .rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
        .defaultIfEmpty(-1L)
        .map(rank -> rank >= 0 ? rank + 1 : rank);
  }

  @Scheduled(initialDelay = 5000, fixedDelay = 10000)
  public void scheduleAllowUser() {
    if (!scheduling) {
      return;
    }
    var maxAllowUserCount = 3L;

    reactiveRedisTemplate.scan(ScanOptions.scanOptions()
            .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
            .count(100)
            .build())
        .map(key -> key.split(":")[2])
        .flatMap(
            queue -> allowUser(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed)))
        .doOnNext(tuple -> log.info(
            "Tried %d and allowed %d members of %s queue".formatted(maxAllowUserCount,
                tuple.getT2(), tuple.getT1())))
        .subscribe();
  }
}
