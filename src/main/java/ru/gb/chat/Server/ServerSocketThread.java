package ru.gb.chat.Server;

public class ServerSocketThread extends Thread {

    private final int port;

    ServerSocketThread(String name, int port) {
        super(name);
        this.port = port;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            System.out.println(getName() + " running on port " + port );
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}
