package com.company.business;

import com.company.data.UserQueries;
import com.company.data.model.User;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewUserCreationValidator implements IUserCreationEventListener {

    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{6,12}$";
    private static final String USERPASS_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$";
    private static final String USEREMAIL_REGEX = "^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    @Override
    public String validate(String userNameValidated, String userPassValidated, String userEmailValidated) {

        net.minidev.json.JSONObject msg = new net.minidev.json.JSONObject();

        if (isExistingUserName(userNameValidated)) {
            msg.put("msg", "The username already exists");
            return msg.toString();
        } else if (!validateUserName(userNameValidated)) {
            msg.put("msg", "The provided username is not valid. Please check the tooltip for requirements");
            return msg.toString();
        } else if (!validateUserPass(userPassValidated)) {
            msg.put("msg", "The provided password is not valid. Please check the tooltip for requirements");
            return msg.toString();
        } else if (isExistingUserEmail(userEmailValidated)) {
            msg.put("msg", "The provided email is already registered with another user");
            return msg.toString();
        } else if (!validateUserEmail(userEmailValidated) && !userEmailValidated.isEmpty()) {
            msg.put("msg", "The provided email is not valid. Please check the tooltip for requirements");
            return msg.toString();
        }else {
            return "true";
        }
    }

    public static Boolean isExistingUserName(String userNameValidated) {
        ArrayList<User> users = UserQueries.getAllUsers();
        for (User thisUser : users) {
            if (userNameValidated.equals(thisUser.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isExistingUserEmail(String userEmailValidated) {
        ArrayList<User> users = UserQueries.getAllUsers();
        for (User thisUser : users) {
            if (userEmailValidated.equals(thisUser.getEmailAddress())) {
                return true;
            }
        }
        return false;
    }

    public static Boolean validateUserName(String userNameValidated) {
        Pattern pattern = Pattern.compile(USERNAME_REGEX);
        Matcher matcher = pattern.matcher(userNameValidated);
        return  matcher.matches();
    }

    public static Boolean validateUserPass(String userPassValidated) {
        Pattern pattern = Pattern.compile(USERPASS_REGEX);
        Matcher matcher = pattern.matcher(userPassValidated);
        return  matcher.matches();
    }

    public static Boolean validateUserEmail(String userEmailValidated) {
        Pattern pattern = Pattern.compile(USEREMAIL_REGEX);
        Matcher matcher = pattern.matcher(userEmailValidated);
        return  matcher.matches();
    }
}
