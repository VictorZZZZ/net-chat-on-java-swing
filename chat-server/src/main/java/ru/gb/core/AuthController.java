package ru.gb.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.data.User;
import ru.gb.database.DatabaseCore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class AuthController {
    public static final Logger logger = LogManager.getLogger(AuthController.class);

    HashMap<String, User> users = new HashMap<>();

//    public void init() {
//        for (User user : receiveUsers()) {
//            users.put(user.getLogin(), user);
//        }
//    }

    public String getNickname(String login, String password){

        //User user = users.get(login);
        User user = null;
        try {
            user = DatabaseCore.getUserByLogin(login);
            if (user != null && user.isPasswordCorrect(password)) {
                logger.info("Пользователь {} найден. Пароль соответствует.",user.getNickname());
                return user.getNickname();
            }
            logger.warn("Пользователь {} не найден или пароль не соответствует.",user.getNickname());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    private ArrayList<User> receiveUsers() {
//        ArrayList<User> usersArr = new ArrayList<>();
//        usersArr.add(new User("admin", "admin", "sysroot"));
//        usersArr.add(new User("alex", "123", "alex-st"));
//        return usersArr;
//    }

}
