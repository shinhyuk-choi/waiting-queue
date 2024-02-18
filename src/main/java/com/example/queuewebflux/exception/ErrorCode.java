package com.example.queuewebflux.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
  ALREADY_REGISTERED(HttpStatus.CONFLICT, "User already registered", "UQ-0001");

  private final HttpStatus httpStatus;
  private final String message;
  private final String code;

  public ApplicationException build() {
    return new ApplicationException(httpStatus, message, code);
  }

  public ApplicationException build(Object... args) {
    return new ApplicationException(httpStatus, message.formatted(args), code);
  }

}
