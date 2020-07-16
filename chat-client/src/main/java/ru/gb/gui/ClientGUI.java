package ru.gb.gui;

import ru.gb.net.MessageSocketThread;
import ru.gb.net.MessageSocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClientGUI extends JFrame implements ActionListener,Thread.UncaughtExceptionHandler, MessageSocketThreadListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int POS_X = 500;
    private static final int POS_Y = 200;

    private final JTextArea chatArea = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField ipAddressField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("8181");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);
    private final JTextField loginField = new JTextField("login");
    private final JPasswordField passwordField = new JPasswordField("123");
    private final JButton buttonLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton buttonDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField messageField = new JTextField();
    private final JButton buttonSend = new JButton("Send");

    private final JList<String> listUsers = new JList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private MessageSocketThread messageSocketThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    public ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat");
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setAlwaysOnTop(true);

        //West
        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);
        listUsers.setListData(new String[]{"user1", "user2", "user3", "user4",
                "user5", "user6", "user7", "user8", "user9", "user-with-too-long-name-in-this-chat"});

        //East
        JScrollPane scrollPaneChatArea = new JScrollPane(chatArea);
        scrollPaneUsers.setPreferredSize(new Dimension(150, 0));

        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        //North
        panelTop.add(ipAddressField);
        panelTop.add(portField);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(loginField);
        panelTop.add(passwordField);
        panelTop.add(buttonLogin);

        //South
        panelBottom.add(buttonDisconnect,BorderLayout.WEST);
        panelBottom.add(messageField,BorderLayout.CENTER);
        panelBottom.add(buttonSend,BorderLayout.EAST);

        add(scrollPaneUsers, BorderLayout.EAST);
        add(scrollPaneChatArea, BorderLayout.CENTER);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom,BorderLayout.SOUTH);
        panelBottom.setVisible(false);

        cbAlwaysOnTop.addActionListener(this::actionPerformed);
        buttonSend.addActionListener(this::actionPerformed);
        messageField.addActionListener(this::actionPerformed);
        buttonLogin.addActionListener(this::actionPerformed);
        buttonDisconnect.addActionListener(this::actionPerformed);

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src==cbAlwaysOnTop){
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if(src==buttonSend || src==messageField){
            sendMessage(loginField.getText(), messageField.getText());
        } else if(src==buttonLogin){

            try {

                Socket socket = null;
                System.out.printf("Trying to connect socket %s:%s%n",ipAddressField.getText(),portField.getText());
                socket = new Socket(ipAddressField.getText(),Integer.parseInt(portField.getText()));
                messageSocketThread = new MessageSocketThread(this,"Client " + loginField.getText(),socket);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                showError(ioException.getMessage());
            }
        } else if(src==buttonDisconnect){
            messageSocketThread.sendMessage(MessageSocketThread.DISCONNECT_CLIENT);
        }else {
            throw new RuntimeException("Unsupported action: " + src.getClass());
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace();
        String msg = String.format("Exception in \"%s\": %s %s%n\t %s",
                t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        JOptionPane.showMessageDialog(this, msg, "Exception!", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public void sendMessage(String user, String msg) {
        if (msg.isEmpty()) {
            return;
        }
        //23.06.2020 12:20:25 <Login>: сообщение
        String messageToChat = String.format("%s <%s>: %s%n", sdf.format(Calendar.getInstance().getTime()), user, msg);
        putMessageInChat(user,msg);
        messageField.setText("");
        messageField.grabFocus();
        messageSocketThread.sendMessage(msg);
    }

    private void putMessageInChat(String user, String msg) {
        String messageToChat = String.format("%s <%s>: %s%n", sdf.format(Calendar.getInstance().getTime()), user, msg);
        chatArea.append(messageToChat);
        putIntoFileHistory(user, messageToChat);
    }

    private void putIntoFileHistory(String user, String msg) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(user + "-history.txt", true))) {
            pw.print(msg);
        } catch (FileNotFoundException e) {
            showError(msg);
        }
    }

    private void showError(String errorMsg) {
        JOptionPane.showMessageDialog(this, errorMsg, "Exception!", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onSocketStarted() {
        panelTop.setVisible(false);
        panelBottom.setVisible(true);
        putMessageInChat("Server","Соединение с сервером установлено");
    }

    @Override
    public void onSocketClosed() {
        panelTop.setVisible(true);
        panelBottom.setVisible(false);
    }

    @Override
    public void onMessageReceived(String msg) {
        putMessageInChat("server", msg);
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
        showError(throwable.getMessage());
    }
}
