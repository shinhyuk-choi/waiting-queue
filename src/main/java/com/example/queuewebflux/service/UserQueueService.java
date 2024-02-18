package com.example.queuewebflux.service;

import com.example.queuewebflux.exception.ErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private final String USER_QUEUE_WAIT_KEY = "user:queue:%s:wait";


  public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
    var unixTimestamp = Instant.now().getEpochSecond();
    return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
        .filter(i -> i)
        .switchIfEmpty(Mono.error(ErrorCode.ALREADY_REGISTERED.build()))
        .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
        .map(rank -> rank >=0 ? rank + 1: rank);
  }
}
