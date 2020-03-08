package main.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.controller.DefaultController;
import main.models.Posts;
import main.models.PostsRepository;
import main.models.Users;
import main.models.UsersRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


   public class AuthPostLogIn {

       @JsonProperty
       boolean result;
       @JsonProperty
        Map <Object, Object> user = new HashMap<>();

        public void getAuthInformation (Map<String, String> information, UsersRepository usersRepository, PostsRepository postsRepository, HttpServletRequest request) {
            String eMail = information.get("e_mail");
            String password = information.get("password");
            HttpSession session = request.getSession();

            this.result = false;
            //Выполняем поиск юзера по eMail и password
            for (Users user : usersRepository.findAll()) {
                if (user.geteMail().equals(eMail)) {
                    if (user.getPassword().equals(password)) {
                        //Заполняем Json информацией о пользователе
                        this.result = true;
                        this.user.put("id", user.getId());
                        this.user.put("name", user.getName());
                        this.user.put("photo", user.getPhoto());
                        this.user.put("email", user.geteMail());
                        this.user.put("moderation", user.isModerator());

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
                        this.user.put("moderationCount", moderationCount);
                        this.user.put("settings", user.isModerator());

                        Map<String, Integer> sessionInformation = new HashMap<>();
                        session.setAttribute("name", (int) (Math.random() * 1000)); //Создали случайным образом имя ссесии
                        sessionInformation.put(String.valueOf(session.getAttribute("name")), user.getId()); // Под случайным именем сессии зафиксировали id текущего пользователя
                        DefaultController.setSessionInformation(sessionInformation);
                    }
                }
            }
        }
        public Integer getUserId() {
            return (Integer) this.user.get("id");
        }
   }
