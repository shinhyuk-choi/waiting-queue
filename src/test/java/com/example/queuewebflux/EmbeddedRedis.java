package com.example.queuewebflux;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedis {

  private final RedisServer redisServer;

  public EmbeddedRedis() throws IOException {
    redisServer = new RedisServer(63790);
  }

  @PostConstruct
  public void startRedis() throws IOException {
    redisServer.start();
  }

  @PreDestroy
  public void stopRedis() throws IOException {
    redisServer.stop();
  }
}
