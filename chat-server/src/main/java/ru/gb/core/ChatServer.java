package ru.gb.core;

import ru.gb.chat.common.MessageLibrary;
import ru.gb.net.MessageSocketThreadListener;
import ru.gb.net.ServerSocketThread;
import ru.gb.net.ServerSocketThreadListener;

import java.net.Socket;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {
    private ServerSocketThread serverSocketThread;
    private ClientSessionThread clientSession;
    private ChatServerListener listener;
    private AuthController authController;
    private Vector<ClientSessionThread> clients = new Vector<>();
    private int sessionCounter=0;

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread(this,"Chat-Server-Socket-Thread", port, 2000);
        serverSocketThread.start();
        authController = new AuthController();
        authController.init();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
    }

    @Override
    public void onClientConnected() {
        logMessage("Client connected");
    }

    @Override
    public void onSocketAccepted(Socket socket) {
        //this.clientSession = new ClientSessionThread(this, "ClientSessionThread", socket);
        clients.add(new ClientSessionThread(this, "ClientSessionThread_"+sessionCounter, socket));
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onClientTimeout(Throwable throwable) {

    }

    @Override
    public void onSocketReady() {
        logMessage("Socket ready");
    }

    @Override
    public void onSocketClosed() {
        logMessage("Socket Closed");
    }

    @Override
    public void onMessageReceived(String msg) {
        String[] splitted = splitClientUID(msg);
        String clientGUID=splitted[0];
        String message = splitted[1];
        for (ClientSessionThread clientSession:clients) {
            if (clientSession.isAlive() && clientSession.isAuthorized()) {
                processAuthorizedUserMessage(clientSession,message);
            } else {
                processUnauthorizedUserMessage(clientSession,message);
            }
        }

    }

    private String[] splitClientUID(String msg){
        String[] result=new String[2];
        String[] split = msg.split(":");
        result[0]=split[0];
        int index = msg.indexOf(":");
        String subString;
        if (index != -1)
        {
            subString= msg.substring(index+1 , msg.length());
            result[1]=subString;
        }
        return result;
    }

    private void processAuthorizedUserMessage(ClientSessionThread clientSession,String msg) {
        logMessage(msg);
        clientSession.sendMessage("echo: " + msg);
    }

    private void processUnauthorizedUserMessage(ClientSessionThread clientSession,String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        if (arr.length < 4 ||
                !arr[0].equals(MessageLibrary.AUTH_METHOD) ||
                !arr[1].equals(MessageLibrary.AUTH_REQUEST)) {
            clientSession.authError("Incorrect request: " + msg);
            return;
        }
        String login = arr[2];
        String password = arr[3];
        String nickname = authController.getNickname(login, password);
        if (nickname == null) {
            clientSession.authDeny();
            return;
        }
        clientSession.authAccept(nickname);
    }

    public void disconnectAll() {
    }

    private void logMessage(String msg) {
        listener.onChatServerMessage(msg);
    }
}
