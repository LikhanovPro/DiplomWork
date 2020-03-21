package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.controller.DefaultController;
import main.models.*;
import main.requestObject.AuthPostLogInObject;
import main.requestObject.AuthPostPasswordObject;
import main.requestObject.AuthPostRegisterObject;
import main.requestObject.AuthPostRestoreObject;
import main.responseObject.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class AuthService implements ResponseApi {

    //Подключаем репозитории
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;
    //======================================================

    //Задаем почту и пароль к eMail Support с которого будет рассылка почты
    private static String supportMail = "DiplomWorkSup@gmail.com";
    private static String supportMailPassword = "123DiplomWork";
    //=======================================================

    @Autowired
    WebProperties webProperties;
    //======================================================

    public ResponseEntity authLogIn (AuthPostLogInObject information, HttpServletRequest request) {
        AuthPostLogIn authPostLogIn = new AuthPostLogIn();

        String eMail = information.getE_mail();
        String password = information.getPassword();
        HttpSession session = request.getSession();

        //Выполняем поиск юзера по eMail и password
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                if (user.getPassword().equals(password)) {
                    //Заполняем Json информацией о пользователе
                    authPostLogIn.setResult(true);
                    authPostLogIn.setUser(userInformation(user));

                    Map<String, Integer> sessionInformation = new HashMap<>();
                    session.setAttribute("name", (int) (Math.random() * 1000)); //Создали случайным образом имя ссесии
                    sessionInformation.put(String.valueOf(session.getAttribute("name")), user.getId()); // Под случайным именем сессии зафиксировали id текущего пользователя
                    DefaultController.setSessionInformation(sessionInformation);
                    return ResponseEntity.status(HttpStatus.OK).body(authPostLogIn);
                }
            }
        }
        authPostLogIn.setResult(false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authPostLogIn);
    }
//--------------------------------------------------------------------------------------------------------------------

    public ResponseEntity authCheck (HttpServletRequest request) {
        AuthGetCheck authGetCheck = new AuthGetCheck();

        HttpSession session = request.getSession();
        Map<String, Integer> sessionInformation = DefaultController.getSessionInformation();

        authGetCheck.setResult(false);
        //Проверка наличия сессии, т.е. есть ли авторизованный пользователь
        for (String key : sessionInformation.keySet()) {
            if (key.equals(String.valueOf(session.getAttribute("name")))) {
                Users user = usersRepository.findById(sessionInformation.get(key)).get();
                //Заполняем Json информацией о пользователе
                authGetCheck.setResult(true);
                authGetCheck.setUser(userInformation(user));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(authGetCheck);
    }

//--------------------------------------------------------------------------------------------------------------------

    public ResponseEntity authLogOut (HttpServletRequest request) {
        AuthGetLogOut authGetLogOut = new AuthGetLogOut();

        HttpSession session = request.getSession();
        // Из текущей сессии получаем id авторизованного пользователя и удаляем его из сессии
        DefaultController.getSessionInformation().remove(String.valueOf(session.getAttribute("name")));
        authGetLogOut.setResult(true);
        return ResponseEntity.status(HttpStatus.OK).body(authGetLogOut);
    }
//--------------------------------------------------------------------------------------------------------------------

    public ResponseEntity authRegister (AuthPostRegisterObject information) {
        AuthPostRegister authPostRegister = new AuthPostRegister();
        int codeLength = 6; //Минлимальная длина пароля (password)
        Map<Object, Object> errors = new HashMap<>();

        //Информация в запросе передается в формате Json
        String eMail = information.geteMail();
        String password = information.getPassword();
        String captcha = information.getCaptcha();
        String captchaSecret = information.getCaptcha_secret();

        //Проверка существования уже зарегистрированного Email
        for (Users user : usersRepository.findAll()) {
            if (user.geteMail().equals(eMail)) {
                authPostRegister.setResult(false);
                errors.put("email", "Этот e-mail уже зарегистрирован");
            }
        }

        //Проверка длины кода не менее 6 знаков
        if (password.length() < codeLength) {
            errors.put("password", "Пароль короче 6-ти символов");
            authPostRegister.setResult(false);
        }

        errors.put("captcha", "Ошибка генерации кода captcha");
        authPostRegister.setResult(false);

        //Проверка соответствия captcha кода, который создается автоматически и записывается в БД
        for (CaptchaCodes captchaCodes : captchaCodesRepository.findAll()) {
            if (captchaCodes.getSecretCode().equals(captchaSecret)) {
                if (captchaCodes.getCode().equals(captcha)) {
                    Users newUser = new Users();
                    newUser.seteMail(eMail);
                    newUser.setModerator(false);
                    newUser.setName(eMail); //В форме регистрации нет поля name, предполагаю регистрацию с именем равным eMail
                    //а затем уже пользователь может его изменить
                    newUser.setPassword(password);
                    newUser.setRegTime(new Date());
                    usersRepository.save(newUser);
                    authPostRegister.setResult(true);
                    errors.clear();
                }
                else {
                    authPostRegister.setResult(false);
                    errors.put("captcha", "Код с картинки введен неверно");
                }
            }
        }
        authPostRegister.setErrors(errors);
        return ResponseEntity.status(HttpStatus.OK).body(authPostRegister);
    }
//--------------------------------------------------------------------------------------------------------------------



    public ResponseEntity createCaptcha() throws IOException {
        AuthGetCaptcha authGetCaptcha = new AuthGetCaptcha();
        int lifeTime = webProperties.getLifeTime();

        //Проверка устаревания уже имеющихся кодов captcha
        captchaCodesRepository.findAll().forEach(captchaCodes -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -lifeTime);//текущая дата минус время жизни captcha кода
            Date date = calendar.getTime();
            if (captchaCodes.getTime().before(date)) {
                captchaCodesRepository.delete(captchaCodes);//Удаление устаревшего кода
            }
        });

        CaptchaCodes captchaCodes = new CaptchaCodes();
        captchaCodes.setTime(new Date()); // Устанавливаем дату создания кода, что бы следить за устареванием
        //Создание Captcha кода
        //Так как каптча оказалась слишком большой по размеру, введено масштабирование в 2 раза
        ArrayList<String> answerArray = new ArrayList<>();
        int scale = 2;
        int codeLength = 4;
        int secretCodeLength = 22;
        StringBuilder sbCode = new StringBuilder();
        StringBuilder sbSecretCode = new StringBuilder();
        Random random = new Random();
        Cage cage = new GCage();
        //Возможные символы для кода
        final char [] ELEMENTSFORCODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        //Возможные символы для секретного кода
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

        captchaCodes.setCode(sbCode.toString()); //Получаем сам код
        captchaCodes.setSecretCode(sbSecretCode.toString()); //Получаем секретный код
        authGetCaptcha.setSecret(sbSecretCode.toString());

        BufferedImage image = cage.drawImage(sbCode.toString()); // Изначально созданный каптч
        BufferedImage result = new BufferedImage(image.getWidth()/scale, image.getHeight()/scale, image.getType()); // Заготовка под вдвое меньший масштаб
        Graphics2D graphics2D = (Graphics2D) result.getGraphics();
        graphics2D.scale(0.5, 0.5);
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose(); // Масштабирование
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result, "png", baos);
        String data = DatatypeConverter.printBase64Binary(baos.toByteArray()); // Преобразование в Base64
        authGetCaptcha.setImage("data:result/png;base64," + data); // Добавка, что бы читалось с HTML

        captchaCodesRepository.save(captchaCodes);

        return ResponseEntity.status(HttpStatus.OK).body(authGetCaptcha);
    }
//--------------------------------------------------------------------------------------------------------------------

    public ResponseEntity authRestorePassword (AuthPostRestoreObject information) {
        AuthPostRestore authPostRestore = new AuthPostRestore();

        String eMail = information.getEmail(); // получаем eMail пользователя, чей пароль будет восстанавливаться

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

        authPostRestore.setResult(false);

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
                authPostRestore.setResult(true);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(authPostRestore);
    }
//--------------------------------------------------------------------------------------------------------------------

    public ResponseEntity authChagePassword (AuthPostPasswordObject information) {
        AuthPostPassword authPostPassword = new AuthPostPassword();
        Map<Object, Object> errors = new HashMap<>();
        int codeLength = webProperties.getCodeLength();

        //Информация с frontend приходит в виде Json файла
        String code = information.getCode();
        String password = information.getPassword();
        String captcha = information.getCaptcha();//Captcha код, который ввел пользователь
        String captchaSecret = information.getCaptcha_secret();//Секретный код captcha, переданный с frontend

        //Проверка длины пароля
        if (password.length() < codeLength) {
            authPostPassword.setResult(false);
            errors.put("password", "Пароль короче 6-ти символов");
        }

        //Проверка соответствия captcha кода
        for (CaptchaCodes captchaCodes : captchaCodesRepository.findAll()) {
            if (captchaCodes.getSecretCode().equals(captchaSecret)) {//поиск в БД captcha кода по секретному captcha коду, переданному с frontend
                if (!captchaCodes.getCode().equals(captcha)) {//Проверка соответствия введенного captcha, хранящемуся в БД
                    authPostPassword.setResult(false);
                    errors.put("captcha", "Код с картинки введен неверно");
                }
            }
        }

        //Если предыдущее условие выполнено, т.е. captcha введен верно
        //Проверем код восстановления пользователя, хранящегося в БД коду восстановления, переданному с frontend
        for (Users user : usersRepository.findAll()) {
            //Добавлена проверка на null, т.к. возникали ошибки при переборе юзеров
            if (user.getCode() != null && user.getCode().equals(code)) {
                user.setPassword(password);
                usersRepository.save(user);
                authPostPassword.setResult(true);
            } else {
                authPostPassword.setResult(false);
                errors.put("code", "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">Запросить ссылку снова</a>");
            }
        }
        authPostPassword.setErrors(errors);
        return ResponseEntity.status(HttpStatus.OK).body(authPostPassword);
    }


//--------------------------------------------------------------------------------------------------------------------
    private Map <Object, Object> userInformation (Users user) {
        Map <Object, Object> userInformation = new HashMap<>();
        userInformation.put("id", user.getId());
        userInformation.put("name", user.getName());
        userInformation.put("photo", user.getPhoto());
        userInformation.put("email", user.geteMail());
        userInformation.put("moderation", user.isModerator());

        int moderationCount = 0;
        //Считаем количество постов, которые были отмодерированы
        for (Posts post : postsRepository.findAll()) {
            try {//Для новых публикаций модератор не определен, поэтому перехватываю исключение NullPointerException
                if (post.getModeratorId() == user.getId()) {
                    moderationCount++;
                }
            } catch (NullPointerException ex){
                System.out.println("Для этого поста модератор не определен");
            }
        }
        userInformation.put("moderationCount", moderationCount);
        userInformation.put("settings", user.isModerator());


        return userInformation;
    };
}
