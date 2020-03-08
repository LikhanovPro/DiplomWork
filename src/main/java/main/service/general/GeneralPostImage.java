package main.service.general;

import main.controller.DefaultController;
import main.models.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GeneralPostImage {

    String pathToImage;

    public GeneralPostImage (HttpServletRequest request, String image, UsersRepository usersRepository){

        Random random = new Random();//Случайности для генерации имен подпапок
        Integer userId = DefaultController.getIdUserLogin(request);//Получаем id текущего пользователя

        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        String userName = usersRepository.findById(userId).get().getName(); //Получаем имя пользоателя, использовал для создания имен подпапок
        StringBuilder pathToFolderWithImage = new StringBuilder();
        pathToFolderWithImage.append("src/main/resources/upload/");
        //Создадим дочерние подпапки из имени Юзера
        int maxLevelOfDirectory = 3; //Уровень конечной подпапки
        //Создаем путь к конечной подпапке
        for (int i = 0; i < maxLevelOfDirectory; i ++) {
            pathToFolderWithImage.append(userName.charAt(random.nextInt(userName.length())));
            pathToFolderWithImage.append("/");
        }
        pathToFolderWithImage.append(image);
        File imageFile = new File(image); // Файл с картинкой, передан с frontend
        BufferedImage bi = null;
        //пересохраняем картинку в нашу подпапку
        try {
            bi = ImageIO.read(imageFile); //Читаю файл с картинкой
            ImageIO.write(bi, "png", new File(String.valueOf(pathToFolderWithImage))); //Записываю картинку в нашу подпапку с форматом png
        } catch (IOException e) {
            e.printStackTrace();
        }
        pathToImage = pathToFolderWithImage.toString();
    }
    public String getPathToImage () {
        return this.pathToImage;
    }
}
