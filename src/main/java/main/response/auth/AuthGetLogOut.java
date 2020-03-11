package main.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class AuthGetLogOut {

    @JsonProperty
    boolean result;

    public AuthGetLogOut () {    }

    private AuthGetLogOut (HttpServletRequest request) {
        HttpSession session = request.getSession();
        // Из текущей сессии получаем id авторизованного пользователя и удаляем его из сессии
        DefaultController.getSessionInformation().remove(String.valueOf(session.getAttribute("name")));
        result = true;
    }

    public ResponseEntity getAuthGetLogOut (HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new AuthGetLogOut(request));
    }
}
