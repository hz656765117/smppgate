package com.hz.smsgate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmsGateApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmsGateApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SmsGateApplication.class, args);
	}
}
