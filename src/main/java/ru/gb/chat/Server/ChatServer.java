package ru.gb.chat.Server;

import java.sql.SQLOutput;

public class ChatServer {

    public void start(int port) {
        System.out.println("Server started on port: " + port);
    }

    public void stop(){
        System.out.println("Server stopped");
    }
}
