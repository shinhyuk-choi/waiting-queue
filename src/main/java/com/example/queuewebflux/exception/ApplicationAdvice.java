package com.example.queuewebflux.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApplicationAdvice {

  @ExceptionHandler(ApplicationException.class)
  Mono<ResponseEntity<ServerExceptionResponse>> applicationExceptionHandler(ApplicationException e) {
    return Mono.just(
        ResponseEntity
            .status(e.getHttpStatus())
            .body(new ServerExceptionResponse(e.getMessage(), e.getCode())));
  }

  public record ServerExceptionResponse(String message, String code) {

  }
}
