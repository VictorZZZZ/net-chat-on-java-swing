package ru.gb.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.gb.chat.common.MessageLibrary;
import ru.gb.database.DatabaseCore;
import ru.gb.net.MessageSocketThread;
import ru.gb.net.MessageSocketThreadListener;
import ru.gb.net.ServerSocketThread;
import ru.gb.net.ServerSocketThreadListener;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {
    private static final Logger logger = LogManager.getLogger(ChatServer.class);

    private ServerSocketThread serverSocketThread;
    private ChatServerListener listener;
    private AuthController authController;
    private Vector<ClientSessionThread> clients = new Vector<>();

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
        logger.info("Сервер запущен");
        //authController.init();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
        disconnectAll();
        logger.info("Сервер остановлен");
    }

    @Override
    public void onClientConnected() {
        logMessage("Client connected");
    }

    @Override
    public void onSocketAccepted(Socket socket) {
        clients.add( new ClientSessionThread(this, "ClientSessionThread", socket));
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onException(MessageSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onClientTimeout(Throwable throwable) {

    }

    @Override
    public void onSocketReady(MessageSocketThread thread) {
        logger.info("Socket ready");
    }

    @Override
    public void onSocketClosed(MessageSocketThread thread) {
        ClientSessionThread clientSession = (ClientSessionThread) thread;
        logger.info("Socket Closed");
        clients.remove(thread);
        if (clientSession.isAuthorized() && !clientSession.isReconnected()) {
            sendToAllAuthorizedClients(MessageLibrary.getBroadcastMessage("server", "User " + clientSession.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList()));
    }

    @Override
    public void onMessageReceived(MessageSocketThread thread,String msg) {
        ClientSessionThread clientSession = (ClientSessionThread)thread;
        if (clientSession.isAuthorized()) {
            processAuthorizedUserMessage(msg);
        } else {
            processUnauthorizedUserMessage(clientSession, msg);
        }
    }

    private void processAuthorizedUserMessage(String msg) {
        logMessage(msg);
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        if(arr.length==3 && arr[0].equals(MessageLibrary.CHANGE_NICKNAME_METHOD)){
            //замена никнейма пользователем
            for (ClientSessionThread client : clients) {
                if(client.getNickname().equals(arr[1]) && DatabaseCore.changeNickname(arr[1],arr[2])){
                    client.setNickname(arr[2]);
                    client.sendMessage(MessageLibrary.getChangeNicknameMessage(arr[1],arr[2]));
                    sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList()));
                    sendToAllAuthorizedClients(MessageLibrary.getBroadcastMessage("server", "Пользователь "+arr[1]+" поменял Никнейм на "+arr[2]));
                }
            }
        } else {
            //рассылка сообщения всем пользователем
            for (ClientSessionThread client : clients) {
                if (!client.isAuthorized()) {
                    continue;
                }
                client.sendMessage(msg);
            }
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        logger.info("Рассылка сообщения:{}",msg);
        for (ClientSessionThread client : clients) {
            if(!client.isAuthorized()) {
                continue;
            }
            client.sendMessage(msg);
        }
    }

    private void processUnauthorizedUserMessage(ClientSessionThread clientSession, String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        if (arr.length < 4 ||
                !arr[0].equals(MessageLibrary.AUTH_METHOD) ||
                !arr[1].equals(MessageLibrary.AUTH_REQUEST)) {
            clientSession.authError("Incorrect request: " + msg);
            logger.warn("Incorrect request: {}",msg);
            return;
        }
        String login = arr[2];
        String password = arr[3];
        String nickname = authController.getNickname(login, password);
        if (nickname == null) {
            clientSession.authDeny();
            return;
        } else {
            ClientSessionThread oldClientSession = findClientSessionByNickname(nickname);
            clientSession.authAccept(nickname);
            if (oldClientSession == null) {
                sendToAllAuthorizedClients(MessageLibrary.getBroadcastMessage("Server", nickname + " connected"));
            } else {
                oldClientSession.setReconnected(true);
                clients.remove(oldClientSession);
            }
        }
        sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList()));
    }

    public String getUsersList() {
        StringBuilder sb = new StringBuilder();
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            sb.append(client.getNickname()).append(MessageLibrary.DELIMITER);
        }
        return sb.toString();
    }

    private ClientSessionThread findClientSessionByNickname(String nickname) {
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            if (client.getNickname().equals(nickname)) {
                return client;
            }
        }
        return null;
    }

    public void disconnectAll() {
        ArrayList<ClientSessionThread> currentClients = new ArrayList<>(clients);
        for (ClientSessionThread client : currentClients) {
            client.close();
            clients.remove(client);
        }
        logger.info("Все клиенты отключены");
    }

    private void logMessage(String msg) {
        listener.onChatServerMessage(msg);
    }
}
