package main.service.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.GlobalSettingRepository;
import main.models.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;

public class GeneralGetSetting {

    @JsonProperty
    boolean MULTIUSER_MODE;

    @JsonProperty
    boolean POST_PREMODERATION;

    @JsonProperty
    boolean STATISTICS_IS_PUBLIC;

    public GeneralGetSetting (HttpServletRequest request, UsersRepository usersRepository, GlobalSettingRepository globalSettingRepository) {
        int idMULTIUSER_MODE = 1;
        int idPOST_PREMODERATION = 2;
        int idSTATISTICS_IS_PUBLIC = 3;
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                MULTIUSER_MODE = globalSettingRepository.findById(idMULTIUSER_MODE).get().getValue();
                POST_PREMODERATION = globalSettingRepository.findById(idPOST_PREMODERATION).get().getValue();
                STATISTICS_IS_PUBLIC = globalSettingRepository.findById(idSTATISTICS_IS_PUBLIC).get().getValue();
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
    }
}
