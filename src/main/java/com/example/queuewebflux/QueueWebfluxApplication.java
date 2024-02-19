package com.example.queuewebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QueueWebfluxApplication {

  public static void main(String[] args) {
    SpringApplication.run(QueueWebfluxApplication.class, args);
  }

}
