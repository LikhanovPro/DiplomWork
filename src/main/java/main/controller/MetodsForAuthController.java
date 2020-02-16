package main.controller;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.models.Posts;
import main.models.PostsRepository;
import main.models.Users;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


public class MetodsForAuthController {

    public static Map<Object, Object> createAuthInformation (Users user, PostsRepository postsRepository) {
        Map <Object, Object> answerJson = new HashMap<Object, Object>();
        Map <Object, Object> userMap = new HashMap<>();

        answerJson.put("result" , true);
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("photo", user.getPhoto());
        userMap.put("email", user.geteMail());
        userMap.put("moderation", user.isModerator());

        int moderationCount = 0;
        for (Posts post : postsRepository.findAll()) {
            if (post.getModeratorId() == user.getId()) {
                moderationCount++;
            }
        }
        userMap.put("moderationCount", moderationCount);
        userMap.put("settings", user.isModerator());
        answerJson.put("user", userMap);
        return answerJson;
    }

    public static ArrayList<String> metodsCreateCaptcha () throws IOException {
        //Так как каптча оказалась слишком большой по размеру, я ввел масштабирование в 2 раза
        ArrayList <String> answerArray = new ArrayList<>();
        int scale = 2;
        int codeLength = 4;
        int secretCodeLength = 22;
        StringBuilder sbCode = new StringBuilder();
        StringBuilder sbSecretCode = new StringBuilder();
        Random random = new Random();
        Cage cage = new GCage();
        final char [] ELEMENTSFORCODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final char [] ELEMENTSFORSECRETCODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        //create code
        for (int i = 0; i < codeLength; i++) {
            sbCode.append(ELEMENTSFORCODE[random.nextInt(ELEMENTSFORCODE.length-1)]);
        }
        for (int j = 0; j < secretCodeLength; j++) {
            sbSecretCode.append(ELEMENTSFORSECRETCODE[random.nextInt(ELEMENTSFORSECRETCODE.length)]-1);
        }
        answerArray.add(sbCode.toString());
        answerArray.add(sbSecretCode.toString());

        BufferedImage image = cage.drawImage(sbCode.toString()); // Изначально созданный каптч
        BufferedImage result = new BufferedImage(image.getWidth()/scale, image.getHeight()/scale, image.getType()); // Заготовка под вдвое меньший масштаб
        Graphics2D graphics2D = (Graphics2D) result.getGraphics();
        graphics2D.scale(0.5, 0.5);
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose(); // Масштабирование
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result, "png", baos);
        String data = DatatypeConverter.printBase64Binary(baos.toByteArray()); // Преобразование в Base64
        answerArray.add("data:result/png;base64," + data); // Добавка, что бы читалось с HTML

        return answerArray;
    }

    public static String generateRestoreCode () {
        StringBuilder code = new StringBuilder();
        int codeLength = 27;
        Random random = new Random();
        final char [] ELEMENTSFORRESTORECODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        for (int i = 0; i < codeLength; i++) {
            code.append(ELEMENTSFORRESTORECODE[random.nextInt(ELEMENTSFORRESTORECODE.length-1)]);
        }
        return code.toString();
    }

    public static void sendMail (String eMail, String restoreCode, String supportMail, String supportMailPassword) {
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
    }

}
