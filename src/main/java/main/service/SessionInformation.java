package main.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service
public class SessionInformation {
    private static Map<String, Integer> sessionInformation = new HashMap<>();

    public static Map<String, Integer> getSessionInformation() {
        return sessionInformation;
    }
    public static void setSessionInformation(Map<String, Integer> information) {
        sessionInformation = information;
    }

    public static Integer getIdUserLogin (HttpServletRequest request) {
        HttpSession session = request.getSession();
        return sessionInformation.get(String.valueOf(session.getAttribute("name")));
    }
}
