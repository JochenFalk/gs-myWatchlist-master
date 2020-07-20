package com.company.business;

import com.company.data.PostgreSystemQueries;
import com.company.data.model.*;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailBusiness {

    private static long emailCount = 0;

    private static final EmailConfig emailConfig = new EmailConfig();
    private static final ApplicationConfig applicationConfig = new ApplicationConfig();
    private static final String BASE_URL = applicationConfig.getHost() + applicationConfig.getPort();
    private static final String CONFIRMATION_URL = "/verifyRegistration?token=";

    public static void sendConfirmationEmail(User user) {

        emailCount = PostgreSystemQueries.getEmailCount();
        String emailAddress = user.getEmailAddress();
        String token = user.getToken();
        String subject = "Registration Confirmation";
        String body = "Thank you for registering. Please click on the below link to activate your account.";
        String htmlBody = "<strong>" + body + "</strong><br><br><a href=" + BASE_URL + CONFIRMATION_URL + token + ">Click here to activate your account</a>";

        ConfirmationEmail email = new ConfirmationEmail(emailCount, emailAddress, subject, htmlBody);

        sendEmail(email, user);
    }

    public static void resendConfirmationEmail(User user) {

        user.setToken();
        user.setTokenExpiryDate();

        emailCount = PostgreSystemQueries.getEmailCount();
        String emailAddress = user.getEmailAddress();
        String token = user.getToken();
        String subject = "Validation link request";
        String body = "This email was sent because a new account validation link was requested. Please click on the below link to activate your account.";
        String htmlBody = "<strong>" + body + "</strong><br><br><a href=" + BASE_URL + CONFIRMATION_URL + token + ">Click here to activate your account</a>";

        ConfirmationEmail email = new ConfirmationEmail(emailCount, emailAddress, subject, htmlBody);

        int validationAttempts = user.getValidationAttempts();
        validationAttempts++;
        user.setValidationAttempts(validationAttempts);
        PostgreSystemQueries.updateUser(user);

        sendEmail(email, user);
    }

    private static void sendEmail(Email email, User user) {

        Session session = Session.getDefaultInstance(emailConfig.getMailProperties(), new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getUserName(), emailConfig.getUserPass());
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("do-not-reply"));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(email.getRecipient()));
            message.setSubject(email.getSubject());
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
            message.setText(email.getBody());
            message.setContent(email.getBody(), "text/html");
            Transport.send(message);

            insertEmails(email, user);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static void insertEmails(Email email, User user) {
        String userName = user.getName();
        long userId = 0;
        User newUser = PostgreSystemQueries.getUserByName(userName);
        if (newUser != null) {
            userId = newUser.getId();
            // known issue: replace dirty while loop with interface callback
            while (userId == 0) {
                User checkUser = PostgreSystemQueries.getUserByName(userName);
                if (checkUser != null) {
                    newUser.setId(checkUser.getId());
                    userId = checkUser.getId();
                }
            }
        }
        PostgreSystemQueries.insertEmail(email, newUser);
    }
}
