package main.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import main.controller.MetodsForAuthController;
import main.models.Users;
import main.models.UsersRepository;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class AuthPostRestore {

    @JsonProperty
    boolean result;

    public void restorePassword(Map<String, String> information, String supportMail, String supportMailPassword, UsersRepository usersRepository) {
        String eMail = information.get("email"); // получаем eMail пользователя, чей пароль будет восстанавливаться

        //Создаем код восстановления
        StringBuilder code = new StringBuilder();
        int codeLength = 27;
        Random random = new Random();
        //Возможные символы для ссылки восстановления пароля
        final char [] ELEMENTSFORRESTORECODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        for (int i = 0; i < codeLength; i++) {
            code.append(ELEMENTSFORRESTORECODE[random.nextInt(ELEMENTSFORRESTORECODE.length-1)]);
        }
        String restoreCode = code.toString(); //получаем случайно сгенерированный код восстановления пароля

        this.result = false;

        //Ищем пользователя в БД по его eMail
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                user.setCode(restoreCode);
                //Если пользователь найден, то отправляем ему на почту ссылку восстановления пароля
                String restorePasswordLink = "http://localhost:8080/login/change-password/" + restoreCode; // Условно заданная ссылка на сайт
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");
                Session session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(supportMail, supportMailPassword);
                    }
                });

                try {
                    Message message = new MimeMessage(session);
                    //от кого
                    message.setFrom(new InternetAddress(supportMail));
                    //кому
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(eMail));
                    //тема сообщения
                    message.setSubject("Восстановление пароля");
                    //текст
                    message.setText(restorePasswordLink);

                    //отправляем сообщение
                    Transport.send(message);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
                usersRepository.save(user);//Сохраняем в БД для пользователя секретный код восстановления пароля
                result = true;
            }
        }
    }
}
