package com.example.socket.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocketClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocketClientApplication.class, args);
	}

}
