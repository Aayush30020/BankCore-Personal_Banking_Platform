package com.bankcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BankcoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				BankcoreApplication.class,
				args
		);
	}
}