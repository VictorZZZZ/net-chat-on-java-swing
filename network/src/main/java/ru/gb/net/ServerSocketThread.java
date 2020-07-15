package ru.gb.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;
    private final ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(timeout);
            System.out.println(getName() + " running on port " + port );
            while (!isInterrupted()) {
                System.out.println("Waiting for connect");
                try {
                    Socket socket = serverSocket.accept();
                    listener.onSocketAccepted(socket);
                }catch (SocketTimeoutException e){
                    listener.onClientTimeout(e);
                    continue;
                }
                listener.onClientConnected();
            }
        } catch (IOException e) {
            listener.onException(e);
        }
    }

}
