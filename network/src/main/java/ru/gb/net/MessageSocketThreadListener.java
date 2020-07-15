package ru.gb.net;

public interface MessageSocketThreadListener {

//  void onSocketStarted();
//  void onSocketClosed();
    void onMessageReceived(String msg);
    void onException(Throwable throwable);
}
