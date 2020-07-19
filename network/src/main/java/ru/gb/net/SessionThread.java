package ru.gb.net;

import java.net.Socket;

class SessionThread extends MessageSocketThread {

    public SessionThread(MessageSocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }
}
