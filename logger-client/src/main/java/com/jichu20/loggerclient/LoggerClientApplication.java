package com.jichu20.loggerclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jichu20")
public class LoggerClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggerClientApplication.class, args);
	}

}
