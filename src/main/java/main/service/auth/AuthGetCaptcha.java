package main.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cage.Cage;
import com.github.cage.GCage;
import com.google.gson.Gson;
import main.controller.MetodsForAuthController;
import main.models.CaptchaCodes;
import main.models.CaptchaCodesRepository;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class AuthGetCaptcha {

    @JsonProperty
    String secret;

    @JsonProperty
    String image;

    //Продолжительность жизни captcha кода в часах
    int lifeTime = 1;

    //Позиция необходимой информации в файле возвращаемом методом: ArrayList <String> createdCaptcha = MetodsForAuthController.metodsCreateCaptcha();
    /*int codeIndex = 0;
    int secretCodeIndex = 1;
    int captchaIndex = 2;*/


    public void createCaptcha(CaptchaCodesRepository captchaCodesRepository) throws IOException {

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
        ArrayList <String> answerArray = new ArrayList<>();
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
        this.secret = sbSecretCode.toString();

        BufferedImage image = cage.drawImage(sbCode.toString()); // Изначально созданный каптч
        BufferedImage result = new BufferedImage(image.getWidth()/scale, image.getHeight()/scale, image.getType()); // Заготовка под вдвое меньший масштаб
        Graphics2D graphics2D = (Graphics2D) result.getGraphics();
        graphics2D.scale(0.5, 0.5);
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose(); // Масштабирование
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result, "png", baos);
        String data = DatatypeConverter.printBase64Binary(baos.toByteArray()); // Преобразование в Base64
        //answerArray.add("data:result/png;base64," + data); // Добавка, что бы читалось с HTML

        this.image = "data:result/png;base64," + data; // Добавка, что бы читалось с HTML

        captchaCodesRepository.save(captchaCodes);

        //answerJson.put("secret", createdCaptcha.get(secretCodeIndex)); //Возвращаем секретный код от сервера на frontend
        //answerJson.put("image", createdCaptcha.get(captchaIndex)); //Возвращаем картинку(зашифрованный код) на frontend для отображения на странице
        //return new Gson().toJson(answerJson);
    }
}
