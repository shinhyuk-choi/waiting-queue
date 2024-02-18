package com.example.queuewebflux.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
  private HttpStatus httpStatus;
  private String message;
  private String code;

}
