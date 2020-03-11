package main.response.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.Users;
import main.models.UsersRepository;
import main.requestObject.general.GeneralPostMProfileObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class GeneralPostMyProfile {

    @Autowired
    private UsersRepository usersRepository;

    @JsonProperty
    boolean result;

    private GeneralPostMyProfile (HttpServletRequest request, GeneralPostMProfileObject information) {

        Integer userId = DefaultController.getIdUserLogin(request);

        //Извлечение информации из Json файла, переданного с frontend
        String photo =information.getPhoto();
        boolean removePhoto = information.isRemovePhoto();
        String name = information.getName();
        String eMail = information.geteMail();
        String password = information.getPassword();

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

    public GeneralPostMyProfile () { }

    public ResponseEntity changeMyProfile (HttpServletRequest request, GeneralPostMProfileObject information) {
        return ResponseEntity.status(HttpStatus.OK).body(new GeneralPostMyProfile(request, information));
    }

}
