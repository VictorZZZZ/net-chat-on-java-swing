package ru.gb.chat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Logger {
    private static final String logfile = "chat_log.txt";
    public static void add(String str){
        try {
            OutputStream os = new FileOutputStream(logfile,true);
            os.write(str.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
