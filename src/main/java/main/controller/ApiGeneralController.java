package main.controller;

import com.google.gson.Gson;
import main.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ApiGeneralController {

    //Подключаем репозитории
    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostCommentsRepository postCommentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GlobalSettingRepository globalSettingRepository;
    //=====================================================

    //Передаем на frontend ощую информацию о создателе сайта
    @GetMapping ("/api/init")
    public static String blogInformation () {
        Gson gson = new Gson();
        Map <String, String> map = new HashMap<>();
        map.put("title", "DevPub");
        map.put("subtitle", "Рассказы разработчиков");
        map.put("phone", "+7 903 666-44-55");
        map.put("email", "mail@mail.ru");
        map.put("copyright", "Дмитрий Сергеевич");
        map.put("copyrightFrom", "2005");
        String json = gson.toJson(map);
        return json;
    }

    //передаем на frontend перечень Тэгов
    @GetMapping ("/api/tag")
    public String getTag () {
        Map <String, ArrayList> answerJson = new HashMap<>();
        answerJson.put("tags", MetodsForGeneralController.createMapForTagWithoutQuery(tagsRepository)); //В Json передаем информацию о Тэгах и их весах
        return new Gson().toJson(answerJson);
    }

    //Контроллер годов, в которые были публикации
    @GetMapping ("/api/calendar")
    public String getCalendar (@RequestParam("year") String year) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

        if (year.length() != 4 || year.isEmpty()) {
            return new Gson().toJson(MetodsForGeneralController.createMapForGetCalendar( Integer.parseInt(dateFormat.format(new Date())), postsRepository));
        }
        else {
            return new Gson().toJson(MetodsForGeneralController.createMapForGetCalendar( Integer.parseInt(year), postsRepository));
        }
    }

    //Контроллер создания коментария к посту или к коментарию
    @PostMapping ("/api/comment")
    public String addComment (HttpServletRequest request,
                              @RequestParam("parent_id") Integer parentId,
                              @RequestParam("post_id") int postId,
                              @RequestParam("text") String text) {
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        Map<String, String> errors = new HashMap<>();

        int commentsLength = 2;//Минимальна длина коментария
        Integer userId = ApiAuthController.getIdUserLogin(request);
        //Проверка, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        PostComments postComments = new PostComments();
        //Проверка: коментарий к посту или к коментарию
        if (parentId != null){
            if (postCommentsRepository.findById(parentId).isPresent()) {
                postComments.setParentId(parentId);
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий коментарий не существует.");
            }
        }

        //Проверка существования поста, к которому передаются коментарии
        if (!postsRepository.findById(postId).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Соответствующий пост не существует.");
        }

        //
        if (text.length() < commentsLength || text.isEmpty()) {
            answerJson.put("result", false);
            errors.put("text", "Текст комментария не задан или слишком короткий");
            return new Gson().toJson(answerJson);
        }

        //Заполняем объект коментария информацией
        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setTime(new Date());
        postComments.setComment(text);

        //Сохраняем коментарий в БД, получая его id, который будет автоматически создан
        int postCommentsId = postCommentsRepository.save(postComments).getId();
        answerJson.put("id", postCommentsId); //Заполняем json для возрата на frontend
        return new Gson().toJson(answerJson);
    }

    //Контроллер модерации поста
    @PostMapping ("/api/moderation")
    public String moderationPost (HttpServletRequest request,
                                  @RequestBody Map<String, Object> information){
        Map<Object, Object> answerJson = new HashMap<Object, Object>();
        ModeratorStatus status;
        //Получаем id текущего пользователя
        Integer userId = ApiAuthController.getIdUserLogin(request);
        //Проверка авторизации
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Integer postId = (Integer) information.get("post_id");
        //решение модератора к посту
        String newStatus = (String) information.get("decision");

        Posts post = postsRepository.findById(postId).get();

        if (usersRepository.findById(userId).get().isModerator()){//Проверка, что авторизованный пользователь - модератор
            if (newStatus.equals("accept")) {//новый статус поста
                status = ModeratorStatus.valueOf("ACCEPTED");
                post.setModerationStatus(status);
            }
            if (newStatus.equals("decline")){//новый статус поста
                status = ModeratorStatus.valueOf("DECLINED");
                post.setModerationStatus(status);
            }
            post.setModeratorId(userId);//сохраняем id модераторра
            postsRepository.save(post);
            answerJson.put("result", true);
            return new Gson().toJson(answerJson);

        }
        answerJson.put("result", false);
        return new Gson().toJson(answerJson);
    }

    //Контроллер сохранения картинки
    @PostMapping ("/api/{image}")
    public String safeImage (HttpServletRequest request, @PathVariable String image) {
        Random random = new Random();//Случайности для генерации имен подпапок
        Integer userId = ApiAuthController.getIdUserLogin(request);//Получаем id текущего пользователя

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
        return pathToFolderWithImage.toString();
    }

    //Контроллер вывода на экран статистики пользователя
    @GetMapping("/api/statistics/my")
    public String myStatistics (HttpServletRequest request) {
        Map <Object, Object> answerForJson = new HashMap<>();

        Integer userId = ApiAuthController.getIdUserLogin(request);//Получаем id пользователя
        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        //Ищем пользователя в БД
        for (Users user : usersRepository.findAll()) {
            if (user.getId() == userId) {
                List <Posts> posts = user.getUserPosts();//Получаем перечень постов авторизованного пользователя
                //Получем статистику авторизованного пользователя
                answerForJson = MetodsForGeneralController.postsStatistics(posts);
                break;
            }
        }
        return new Gson().toJson(answerForJson);
    }

    //Контролер вывода статистики по сайту в общем
    @GetMapping("/api/statistics/all")
    public String allStatistics (HttpServletRequest request) {
        Map <Object, Object> answerForJson = new HashMap<>();

        Integer userId = null;
        userId = ApiAuthController.getIdUserLogin(request);

        int idStatisticsIsPublic = 3;//Номер id информации из БД, где указывает о доступе общей статистики неавторизованному пользователю

        //Проверка выполнения условий вывода информации по сайту
        if (!globalSettingRepository.findById(idStatisticsIsPublic).get().getValue() && userId.equals(null)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            List <Posts> posts = new ArrayList<>();
            //Заполнение списка общим список всех постов с сайта
            for (Posts post : postsRepository.findAll()) {
                posts.add(post);
            }
            //Сбор общей информации по всем постам
            answerForJson = MetodsForGeneralController.postsStatistics(posts);
            return new Gson().toJson(answerForJson);
        }
    }

    //Контроллер передачи настроек сайта
    @GetMapping("/api/settings")
    public String getSettings (HttpServletRequest request) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        Map <Object, Object> answerForJson = new HashMap<>();

        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                for (GlobalSetting globalSetting : globalSettingRepository.findAll()) {
                    answerForJson.put(globalSetting.getCode(), globalSetting.getValue());//Передача настроек сайта
                }
            }
        }

        else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        return new Gson().toJson(answerForJson);
    }

    //Контроллер изменения настроек сайта
    @PutMapping ("/api/settings")
    public String saveSettings (HttpServletRequest request) {
        Integer userId = ApiAuthController.getIdUserLogin(request);
        Map <Object, Object> answerForJson = new HashMap<>();

        //проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                for (GlobalSetting globalSetting : globalSettingRepository.findAll()) {
                    answerForJson.put(globalSetting.getCode(), globalSetting.getValue());
                }
            }
        }
        else {
              throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        return new Gson().toJson(answerForJson);
    }

    //Контроллер установки порфиля
    @PostMapping("api/profile/my")
    public String editProfile (HttpServletRequest request,
                               @RequestBody Map<String, Object> information) {

        Map <Object, Object> answerForJson = new HashMap<>();
        Integer userId = ApiAuthController.getIdUserLogin(request);

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
            answerForJson.put("result", true);
        }
        return new Gson().toJson(answerForJson);
    }

}


