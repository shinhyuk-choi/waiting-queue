package com.example.queuewebflux.controller;

import com.example.queuewebflux.dto.RegisterUserResponse;
import com.example.queuewebflux.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {

  private final UserQueueService userQueueService;

  @PostMapping("")
  public Mono<RegisterUserResponse> registerWaitQueue(
      @RequestParam(value = "queue", defaultValue = "default") String queue,
      @RequestParam("user_id") Long userId
  ) {
    return userQueueService.registerWaitQueue(queue, userId)
        .map(RegisterUserResponse::new);
  }

}
