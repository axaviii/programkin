package com.example.socketserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

@Component
public class Server {

    @Value("${app.server.port}")
    private int port;

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
           String request = reader.readLine();
            System.out.println("Received request from client: " + request);

            String response = "Server response: " + UUID.randomUUID();
            writer.println(response);
            System.out.println("Sent response client: " + response);

        }catch (IOException ex){
            ex.printStackTrace();
        }finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
