package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.Users;
import main.models.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class GeneralPostMyProfile {

    @JsonProperty
    boolean result;

    public GeneralPostMyProfile (HttpServletRequest request, Map<String, Object> information, UsersRepository usersRepository) {

        Integer userId = DefaultController.getIdUserLogin(request);

        //Извлечение информации из Json файла, переданного с frontend
        String photo =(String) information.get("photo");
        boolean removePhoto = (boolean) information.get("removePhoto");
        String name = (String) information.get("name");
        String eMail = (String) information.get("email");
        String password = (String) information.get("password");

        //Проверка авторизации пользователя
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            Users user = usersRepository.findById(userId).get();
            if (!photo.isEmpty()) {//Проверка установки фотографии
                user.setPhoto(photo);
            }
            if (removePhoto) {//Проверка необходимости удалить фотографию
                user.setPhoto(null);
            }

            //Сохранение информации о пользователе
            user.setName(name);
            user.seteMail(eMail);
            user.setPassword(password);
            usersRepository.save(user);
            result = true;
        }
    }
}
