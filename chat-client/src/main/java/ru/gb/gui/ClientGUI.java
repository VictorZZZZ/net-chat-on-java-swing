package ru.gb.gui;

import ru.gb.chat.common.MessageLibrary;
import ru.gb.net.MessageSocketThread;
import ru.gb.net.MessageSocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ClientGUI extends JFrame implements ActionListener,Thread.UncaughtExceptionHandler, MessageSocketThreadListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int POS_X = 400;
    private static final int POS_Y = 200;

    private final JTextArea chatArea = new JTextArea();
    private final JPanel panelTop = new JPanel();
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
    private MessageSocketThread socketThread;
    private final String WINDOW_TITLE = "Chat Client";
    private String nickname;

    private final JPanel panelSettings = new JPanel(new BorderLayout());
    private final JTextField fieldChangeNickname = new JTextField("new Nickname");
    private final JButton buttonChangeNickname = new JButton("<html><b>Change Nickname</b></html>");

    private final JPanel panelConnection = new JPanel(new GridLayout(2,3));
    private String historyFile;

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
        setTitle(WINDOW_TITLE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setAlwaysOnTop(true);

        //West
        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);
//        listUsers.setListData(new String[]{"user1", "user2", "user3", "user4",
//                "user5", "user6", "user7", "user8", "user9", "user-with-too-long-name-in-this-chat"});

        //East
        JScrollPane scrollPaneChatArea = new JScrollPane(chatArea);
        scrollPaneUsers.setPreferredSize(new Dimension(150, 0));

        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        //North

        panelSettings.add(fieldChangeNickname,BorderLayout.CENTER);
        panelSettings.add(buttonChangeNickname,BorderLayout.EAST);
        panelSettings.setVisible(false);

        panelConnection.add(ipAddressField);
        panelConnection.add(portField);
        panelConnection.add(cbAlwaysOnTop);
        panelConnection.add(loginField);
        panelConnection.add(passwordField);
        panelConnection.add(buttonLogin);

        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        panelTop.add(panelSettings);
        panelTop.add(panelConnection);



        //South
        panelBottom.add(buttonDisconnect,BorderLayout.WEST);
        panelBottom.add(messageField,BorderLayout.CENTER);
        panelBottom.add(buttonSend,BorderLayout.EAST);

        //add(panelSettings);
        add(scrollPaneUsers, BorderLayout.EAST);
        add(scrollPaneChatArea, BorderLayout.CENTER);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom,BorderLayout.SOUTH);


        cbAlwaysOnTop.addActionListener(this::actionPerformed);
        buttonSend.addActionListener(this::actionPerformed);
        messageField.addActionListener(this::actionPerformed);
        buttonLogin.addActionListener(this::actionPerformed);
        buttonDisconnect.addActionListener(this::actionPerformed);
        buttonChangeNickname.addActionListener(this::actionPerformed);

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src==cbAlwaysOnTop){
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if(src==buttonSend || src==messageField){
            sendMessage(messageField.getText());
        } else if(src==buttonLogin){
            Socket socket = null;
            try {
                socket = new Socket(ipAddressField.getText(), Integer.parseInt(portField.getText()));
                socketThread = new MessageSocketThread(this, "Client" + loginField.getText(), socket);
            } catch (IOException ioException) {
                showError(ioException.getMessage());
            }
        } else if(src == buttonDisconnect) {
            socketThread.close();
            chatArea.setText("");
        }else if(src==buttonChangeNickname){
            changeNickname(fieldChangeNickname.getText());
        }else {
            throw new RuntimeException("Unsupported action: " + src.getClass());
        }
    }

    private void changeNickname(String newNickname) {
        socketThread.sendMessage(MessageLibrary.getChangeNicknameMessage(nickname, newNickname));
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

    public void sendMessage(String msg) {
        if (msg.isEmpty()) {
            return;
        }
        //23.06.2020 12:20:25 <Login>: сообщение
        putMessageInChatArea(nickname,msg);
        messageField.setText("");
        messageField.grabFocus();
        socketThread.sendMessage(MessageLibrary.getTypeBroadcastClient(nickname, msg));
    }

    private void putMessageInChatArea(String user, String msg) {
        String messageToChat = String.format("%s <%s>: %s%n", sdf.format(Calendar.getInstance().getTime()), user, msg);
        chatArea.append(messageToChat);
        putIntoFileHistory(user, messageToChat);
    }

    private void putIntoFileHistory(String user, String msg) {
        if(historyFile!=null) {
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(historyFile, true))) {
                pw.print(msg);
            } catch (FileNotFoundException e) {
                showError(msg);
            }
        }
    }

    private void showError(String errorMsg) {
        JOptionPane.showMessageDialog(this, errorMsg, "Exception!", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onMessageReceived(MessageSocketThread thread, String msg) {
        handleMessage(msg);
    }

    @Override
    public void onException(MessageSocketThread thread,Throwable throwable) {
        throwable.printStackTrace();
        showError(throwable.getMessage());
    }

    @Override
    public void onSocketReady(MessageSocketThread thread) {
        panelConnection.setVisible(false);
        panelSettings.setVisible(true);

        panelBottom.setVisible(true);
        socketThread.sendMessage(MessageLibrary.getAuthRequestMessage(loginField.getText(), new String(passwordField.getPassword())));
    }

    @Override
    public void onSocketClosed(MessageSocketThread thread) {
        panelSettings.setVisible(false);
        panelConnection.setVisible(true);

        panelBottom.setVisible(false);
    }

    private void handleMessage(String msg) {
        String[] values = msg.split(MessageLibrary.DELIMITER);
        switch (MessageLibrary.getMessageType(msg)) {
            case AUTH_ACCEPT:
                this.nickname = values[2];
                setNicknameInTitle();
                historyFile = nickname+"-history.txt";
                fieldChangeNickname.setText(this.nickname);
                showHistory();
                break;
            case AUTH_DENIED:
                putMessageInChatArea("server", msg);
                socketThread.close();
                break;
            case TYPE_BROADCAST:
                putMessageInChatArea(values[2], values[3]);
                break;
            case MSG_FORMAT_ERROR:
                putMessageInChatArea("server", msg);
                break;
            case USER_LIST:
                // /user_list##user1##user2##user3
                String users = msg.substring(MessageLibrary.USER_LIST.length() +
                        MessageLibrary.DELIMITER.length());
                // user1##user2##user3
                String[] userArray = users.split(MessageLibrary.DELIMITER);
                Arrays.sort(userArray);
                listUsers.setListData(userArray);
                break;
            case TYPE_BROADCAST_CLIENT:
                String srcNickname = values[1];
                if (srcNickname.equals(nickname)) {
                    return;
                }
                putMessageInChatArea(srcNickname, values[2]);
                break;
            case CHANGED_NICKNAME:
                nickname=values[2];
                setNicknameInTitle();
                break;
            default:
                throw new RuntimeException("Unknown message: " + msg);

        }
    }

    private void showHistory() {
        Path path = Paths.get(historyFile);
        try {
            long lineCount = Files.lines(path).count();
            if(lineCount<100){
                BufferedReader reader = new BufferedReader(new FileReader(historyFile));
                String str;
                while ((str = reader.readLine()) !=null ) {
                    chatArea.append(str+"\n");
                }
                reader.close();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(historyFile));
                long counter=0;
                String str;
                while ((str = reader.readLine()) !=null ) {
                    counter++;
                    if(counter>lineCount-100){
                        chatArea.append(str+"\n");
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setNicknameInTitle() {
        setTitle(WINDOW_TITLE + " authorized with nickname: " + nickname);
    }
}
