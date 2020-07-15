package ru.gb.core;

import ru.gb.net.MessageSocketThread;
import ru.gb.net.MessageSocketThreadListener;
import ru.gb.net.ServerSocketThread;
import ru.gb.net.ServerSocketThreadListener;

import java.net.Socket;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {

    private static ServerSocketThread serverSocketThread;
    private MessageSocketThread messageSocketThread;

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread(this,"Chat-Server-Socket-Thread" , port,2000);
        serverSocketThread.start();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
    }

    @Override
    public void onClientConnected() {
        System.out.println("Client connected");
    }

    @Override
    public void onSocketAccepted(Socket socket) {
        messageSocketThread = new MessageSocketThread(this,"ServerSocket",socket);
    }

    @Override
    public void onMessageReceived(String msg) {
        System.out.println(msg);
        messageSocketThread.sendMessage("echo: " + msg);
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onClientTimeout(Throwable throwable) {

    }
}
