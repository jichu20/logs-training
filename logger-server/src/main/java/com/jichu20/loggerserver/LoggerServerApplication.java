package com.jichu20.loggerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jichu20")
public class LoggerServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(LoggerServerApplication.class, args);
  }

}
