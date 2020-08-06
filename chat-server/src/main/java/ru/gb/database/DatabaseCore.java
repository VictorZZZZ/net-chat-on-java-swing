package ru.gb.database;

import ru.gb.data.User;

import java.sql.*;

public class DatabaseCore {
    private static Connection connection;
    private static Statement statement;

//    public static void main(String[] args) {
//        try {
//            User user=getUserByLogin("test");
//            System.out.println(user);
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(changeNickname("alex-stt","alex-st"));
//
//    }

    public static boolean changeNickname(String nickname, String newNickname) {

        try {
            connect();
            String query = "UPDATE users SET nickname=? WHERE nickname=?";
            PreparedStatement preparedStatement=connection.prepareStatement(query);
            preparedStatement.setString(1,newNickname);
            preparedStatement.setString(2,nickname);
            boolean result = preparedStatement.executeUpdate()>0;
            disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
            return false;
        }
        

    }

    public static User getUserByLogin(String login) throws SQLException, ClassNotFoundException {
        connect();
        String query = "SELECT * FROM users WHERE login=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,login);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            String userLogin = rs.getString("login");
            String userNickname = rs.getString("nickname");
            String userPassword = rs.getString("password");
            disconnect();
            return new User(userLogin, userPassword, userNickname);
        } else {
            disconnect();
            return null;
        }
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        statement = connection.createStatement();
    }


    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
