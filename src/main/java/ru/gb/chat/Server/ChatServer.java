package ru.gb.chat.Server;

import java.sql.SQLOutput;

public class ChatServer {

    private static ServerSocketThread serverSocketThread;

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread("Chat-Server-Socket-Thread", port);
        serverSocketThread.start();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
    }
}
