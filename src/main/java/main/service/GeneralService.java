package main.service;

import main.controller.DefaultController;
import main.models.*;
import main.requestObject.GeneralPostMProfileObject;
import main.requestObject.GeneralPostModerationObject;
import main.responseObject.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.Document;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GeneralService implements ResponseApi {

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

    //Информация о разработчеке сайта
    String title = "DevPub";
    String subtitle = "Рассказы разработчиков";
    String phone = "+7 903 666-44-55";
    String email = "mail@mail.ru";
    String copyright = "Дмитрий Сергеевич";
    String copyrightFrom = "2005";
    //=====================================================

    @Autowired
    WebProperties webProperties;
    //=====================================================

    public ResponseEntity generalInit () {
        GeneralGetInit generalGetInit = new GeneralGetInit();

        generalGetInit.setTitle(title);
        generalGetInit.setSubtitle(subtitle);
        generalGetInit.setPhone(phone);
        generalGetInit.setEmail(email);
        generalGetInit.setCopyright(copyright);
        generalGetInit.setCopyrightFrom(copyrightFrom);
        return ResponseEntity.status(HttpStatus.OK).body(generalGetInit);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalTags () {
        GeneralGetTag generalGetTag = new GeneralGetTag();

    Map<String, Integer> tagCount = new HashMap<>();
    Set<Integer> tagCounts = new TreeSet<>();
    ArrayList<Map> tags = new ArrayList<Map>();

    //Считаем количество тегов вообще и сколь раз он встречается в постах
    tagsRepository.findAll().forEach(tag -> {
        int i = 0;
        for (Posts post : tag.getPostsForTags()) {
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                i++;
            }
        }
        tagCounts.add(i);
        tagCount.put(tag.getName(), i);
    });
    tagsRepository.findAll().forEach(tag -> {
        Map <Object, Object> tagWeight = new HashMap<>();
        tagWeight.put("name", tag.getName());
        tagWeight.put("weight", (double) tagCount.get(tag.getName())/tagCounts.stream().max(Integer::compareTo).get());//Считаем весса тегов
        tags.add(tagWeight);
    });
    generalGetTag.setTags(tags);
        return ResponseEntity.status(HttpStatus.OK).body(generalGetTag);
}
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalGetCalendar (String year) {
        GeneralGetCalendar generalGetCalendar = new GeneralGetCalendar();
        Set<Integer> years = new TreeSet<>();
        Map <Object, Integer> posts = new HashMap<>();

        SimpleDateFormat dateFormatForYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (year.length() != 4 || year.isEmpty()) {
            year = dateFormat.format(new Date());
        }
        years.add(Integer.parseInt(year));

        postsRepository.findAll().forEach(post -> {
            if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")) {
                years.add(Integer.parseInt(dateFormatForYear.format(post.getTime())));
                if (posts.containsKey(dateFormat.format(post.getTime()))) {
                    posts.replace(dateFormat.format(post.getTime()), posts.get(dateFormat.format(post.getTime())),
                            posts.get(dateFormat.format(post.getTime())) + 1);
                }
                else {
                    posts.put(dateFormat.format(post.getTime()), 1);
                }
            }
        });
        generalGetCalendar.setPosts(posts);
        generalGetCalendar.setYears(years);
        return ResponseEntity.status(HttpStatus.OK).body(generalGetCalendar);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity addComment (HttpServletRequest request, Integer parentId, int postId, String text) {
        GeneralPostComment generalPostComment = new GeneralPostComment();
        Map<String, String> errors = new HashMap<>();
        int commentsLength = webProperties.getCommentsLength();//Минимальна длина коментария

        Integer userId = DefaultController.getIdUserLogin(request);
        //Проверка, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        PostComments postComments = new PostComments();
        postComments.setParentId(null);
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

        //Проверка длины коментария
        if (text.length() < commentsLength || text.isEmpty()) {
            generalPostComment.setResult(false);
            errors.put("text", "Текст комментария не задан или слишком короткий");
            generalPostComment.setErrors(errors);
        }

        //Заполняем объект коментария информацией
        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setTime(new Date());
        postComments.setComment(text);

        generalPostComment.setResult(true);
        //Сохраняем коментарий в БД, получая его id, который будет автоматически создан
        generalPostComment.setId(postCommentsRepository.save(postComments).getId()); //Заполняем json для возрата на frontend

        return ResponseEntity.status(HttpStatus.OK).body(generalPostComment);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalModeration (HttpServletRequest request, GeneralPostModerationObject information) {
        GeneralPostModeration generalPostModeration = new GeneralPostModeration();

        ModeratorStatus status;
        //Получаем id текущего пользователя
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка авторизации
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        Integer postId = information.getPostId();
        //решение модератора к посту
        String newStatus = information.getNewStatus();

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
            generalPostModeration.setResult(true);

        }
        else {
            generalPostModeration.setResult(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalPostModeration);
    }
//---------------------------------------------------------------------------------------------------------------------

    public String generalImage (HttpServletRequest request, MultipartFile image) {
        System.out.println();

        Random random = new Random();//Случайности для генерации имен подпапок
        Integer userId = DefaultController.getIdUserLogin(request);//Получаем id текущего пользователя

        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        String userName = usersRepository.findById(userId).get().getName(); //Получаем имя пользоателя, использовал для создания имен подпапок
        StringBuilder pathToFolderWithImage = new StringBuilder();
        StringBuilder pathToImage = new StringBuilder();
        pathToFolderWithImage.append("src/main/resources/static/upload/");
        //Создадим дочерние подпапки из имени Юзера
        int maxLevelOfDirectory = 3; //Уровень конечной подпапки
        //Создаем путь к конечной подпапке
        for (int i = 0; i < maxLevelOfDirectory; i++) {
            pathToFolderWithImage.append(userName.charAt(random.nextInt(userName.length())));
            pathToFolderWithImage.append("/");
        }
        File newFolder = new File(pathToFolderWithImage.toString());
        if (!newFolder.exists()) {
            System.out.println("Создаем папку");
            newFolder.mkdirs();
        }
        pathToImage.append(pathToFolderWithImage).append("image.jpg");
        //File imageFile = new File("image"); // Файл с картинкой, передан с frontend
        BufferedImage bi = null;
        //пересохраняем картинку в нашу подпапку
        try {
            bi = ImageIO.read(image.getInputStream()); //Читаю файл с картинкой

            ImageIO.write(bi, "jpg", new File(String.valueOf(pathToImage))); //Записываю картинку в нашу подпапку с форматом png
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Создаю относительный путь к файлу с картинкой

        return "src/main/resources/upload/tiger.jpg";
    }

        //return pathToImage.toString().replaceAll("src/main/resources/", "");}
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalMyStatistic (HttpServletRequest request) {
        GeneralGetMyStatistic generalGetMyStatistic = new GeneralGetMyStatistic();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Integer userId = DefaultController.getIdUserLogin(request);//Получаем id пользователя
        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        //Ищем пользователя в БД
        Users user = usersRepository.findById(userId).get();
        List<Posts> posts = user.getUserPosts();//Получаем перечень постов авторизованного пользователя

        //Получем статистику авторизованного пользователя
        generalGetMyStatistic.setPostsCount(posts.size());
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        Date date = new Date();
        for (Posts post : posts) {
            viewsCount += post.getViewCount();
            if (!date.before(post.getTime())) {
                date = post.getTime();
            }
            for (PostsVotes votes : post.getVotesToPost()) {
                if (votes.isValue()) {
                    likesCount++;
                }
                else {
                    dislikesCount++;
                }
            }

        }
        generalGetMyStatistic.setLikesCount(likesCount);
        generalGetMyStatistic.setDislikesCount(dislikesCount);
        generalGetMyStatistic.setViewsCount(viewsCount);
        generalGetMyStatistic.setFirstPublication(dateFormat.format(date));

        return ResponseEntity.status(HttpStatus.OK).body(generalGetMyStatistic);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalAllStatistic (HttpServletRequest request) {
        GeneralGetAllStatistic generalGetAllStatistic = new GeneralGetAllStatistic();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Integer userId = null;
        userId = DefaultController.getIdUserLogin(request);

        int idStatisticsIsPublic = 3;//Номер id информации из БД, где указывает о доступе общей статистики неавторизованному пользователю

        //Проверка выполнения условий вывода информации по сайту
        if (!globalSettingRepository.findById(idStatisticsIsPublic).get().getValue() && userId.equals(null)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            List<Posts> posts = new ArrayList<>();
            //Заполнение списка общим список всех постов с сайта
            for (Posts post : postsRepository.findAll()) {
                posts.add(post);
            }
            //Сбор общей информации по всем постам
            generalGetAllStatistic.setPostsCount(posts.size());
            int likesCount = 0;
            int dislikesCount = 0;
            int viewsCount = 0;
            Date date = new Date();
            for (Posts post : posts) {
                viewsCount += post.getViewCount();
                if (!date.before(post.getTime())) {
                    date = post.getTime();
                }
                for (PostsVotes votes : post.getVotesToPost()) {
                    if (votes.isValue()) {
                        likesCount++;
                    } else {
                        dislikesCount++;
                    }
                }
            }
            generalGetAllStatistic.setLikesCount(likesCount);
            generalGetAllStatistic.setDislikesCount(dislikesCount);
            generalGetAllStatistic.setViewsCount(viewsCount);
            generalGetAllStatistic.setFirstPublication(dateFormat.format(date));
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalGetAllStatistic);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalGetSetting (HttpServletRequest request) {
        GeneralGetSetting generalGetSetting = new GeneralGetSetting();

        int idMULTIUSER_MODE = 1;
        int idPOST_PREMODERATION = 2;
        int idSTATISTICS_IS_PUBLIC = 3;
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                generalGetSetting.setMULTIUSER_MODE(globalSettingRepository.findById(idMULTIUSER_MODE).get().getValue());
                generalGetSetting.setPOST_PREMODERATION(globalSettingRepository.findById(idPOST_PREMODERATION).get().getValue());
                generalGetSetting.setSTATISTICS_IS_PUBLIC(globalSettingRepository.findById(idSTATISTICS_IS_PUBLIC).get().getValue());
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalGetSetting);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalPutSetting (HttpServletRequest request) {
        GeneralPutSetting generalPutSetting = new GeneralPutSetting();

        int idMULTIUSER_MODE = 1;
        int idPOST_PREMODERATION = 2;
        int idSTATISTICS_IS_PUBLIC = 3;
        Integer userId = DefaultController.getIdUserLogin(request);

        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                generalPutSetting.setMULTIUSER_MODE(globalSettingRepository.findById(idMULTIUSER_MODE).get().getValue());
                generalPutSetting.setPOST_PREMODERATION(globalSettingRepository.findById(idPOST_PREMODERATION).get().getValue());
                generalPutSetting.setSTATISTICS_IS_PUBLIC(globalSettingRepository.findById(idSTATISTICS_IS_PUBLIC).get().getValue());
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
       return ResponseEntity.status(HttpStatus.OK).body(generalPutSetting);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity generalMyProfile (HttpServletRequest request, GeneralPostMProfileObject information) {
        GeneralPostMyProfile generalPostMyProfile = new GeneralPostMyProfile();

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
            generalPostMyProfile.setResult(true);
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalPostMyProfile);
    }
}