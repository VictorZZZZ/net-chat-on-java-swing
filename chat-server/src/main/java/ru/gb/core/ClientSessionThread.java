package ru.gb.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.chat.common.MessageLibrary;
import ru.gb.net.MessageSocketThreadListener;
import ru.gb.net.MessageSocketThread;
import java.net.Socket;

public class ClientSessionThread extends MessageSocketThread{
    public static final Logger logger = LogManager.getLogger(ClientSessionThread.class);

    private boolean isAuthorized = false;
    private String nickname;
    private boolean reconnected = false;

    public ClientSessionThread(MessageSocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public String getNickname() {
        return nickname;
    }

    public void authAccept(String nickname) {
        this.nickname = nickname;
        this.isAuthorized = true;
        sendMessage(MessageLibrary.getAuthAcceptMessage(nickname));
        logger.info("Клиент {} залогинился",nickname);
    }

    public void authDeny() {
        sendMessage(MessageLibrary.getAuthDeniedMessage());
        close();
    }

    public void authError(String msg) {
        sendMessage(MessageLibrary.getMsgFormatErrorMessage(msg));
        close();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isReconnected() {
        return reconnected;
    }

    public void setReconnected(boolean reconnected) {
        this.reconnected = reconnected;
    }
}
