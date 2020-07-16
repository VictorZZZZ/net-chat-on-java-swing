package ru.gb.net;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class MessageSocketThread extends Thread {
    public static final String DISCONNECT_CLIENT = "/disconnectClient";
    public static final String DISCONNECT_SERVER = "/disconnectServer";
    private Socket socket;
    private MessageSocketThreadListener listener;

    public MessageSocketThread(MessageSocketThreadListener listener,String name,Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        start();
        if(this.isAlive()){
            listener.onSocketStarted();
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            while (!isInterrupted()){
                System.out.println("Waiting for message");
                String msg = in.readUTF();
                if(msg.equals(DISCONNECT_SERVER)){
                    interrupt();
                } else {
                    listener.onMessageReceived(msg);
                }
            }
            if(isInterrupted()){
                socket.close();
                listener.onSocketClosed();
                System.out.println("Сокет закрыт MessageSocketThread stopped!");
            }
        } catch (IOException e) {
            listener.onException(e);
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            if(!socket.isConnected() || socket.isClosed()){
                listener.onException(new RuntimeException("Socked is Closed or not initialized"));
                return;
            }
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
        } catch (IOException e) {
            listener.onException(e);
        }
    }
}
