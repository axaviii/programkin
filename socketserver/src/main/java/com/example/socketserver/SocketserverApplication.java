package com.example.socketserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SocketserverApplication {

	@Autowired
	private Server server;

	public static void main(String[] args) {
		SpringApplication.run(SocketserverApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startServer() {
		server.start();
	}

}
