package main.service;

import main.models.*;
import main.requestObject.GeneralPostMProfileObject;
import main.requestObject.GeneralPostModerationObject;
import main.responseObject.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GeneralService {

    @Value("${diplomawork.commentsLength}")
    int commentsLength;
    @Value("${diplomawork.pathToImages}")
    String pathToImages;
    @Value("${diplomawork.pathToAvatars}")
    String pathToAvatars;
    @Value("${diplomawork.pathToDefaultAvatar}")
    String pathToDefaultAvatar;
    @Value("${diploma.dateformat.calendar}")
    String dateFormatCalendar;
    @Value("${diploma.dateformat.year}")
    String dateFormatYear;
    @Value("${diploma.dateformat.post}")
    String dateFormatPost;
    @Value("${diploma.db.idStatisticsIsPublic}")
    int idStatisticsIsPublic;
    @Value("${diploma.db.idPostPreModeration}")
    int idPostPreModeration;
    @Value("${diploma.db.idMultiUserMode}")
    int idMultiUserMode;
    @Value("${diploma.maxLevelOfDirectoryToImage}")
    int maxLevelOfDirectory;

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

    @Autowired
    private SessionInformation sessionInformation;
    //=====================================================
    //Информация о разработчеке сайта
    @Value("${diplomawork.iformationaboutsite.title}")
    String title = "DevPub";
    @Value("${diplomawork.iformationaboutsite.subtitle}")
    String subtitle = "Рассказы разработчиков";
    @Value("${diplomawork.iformationaboutsite.phone}")
    String phone = "+7 903 666-44-55";
    @Value("${diplomawork.iformationaboutsite.email}")
    String email = "mail@mail.ru";
    @Value("${diplomawork.iformationaboutsite.copyright}")
    String copyright = "Дмитрий Сергеевич";
    @Value("${diplomawork.iformationaboutsite.copyrightFrom}")
    String copyrightFrom = "2005";
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

    public ResponseEntity <ResponseApi> generalTags () {
        GeneralGetTag generalGetTag = new GeneralGetTag();
        Map<String, Integer> tagCount = new HashMap<>();
        Set<Integer> tagCounts = new TreeSet<>();
        ArrayList<Map> tags = new ArrayList<Map>();
        //Считаем количество тегов и сколько раз он встречается в постах
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
            tagWeight.put("weight", (double) tagCount.get(tag.getName())/tagCounts.stream().max(Integer::compareTo).get());//Считаем веса тегов
            tags.add(tagWeight);
        });
        generalGetTag.setTags(tags);
            return ResponseEntity.status(HttpStatus.OK).body(generalGetTag);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> generalGetCalendar (String year) {
        GeneralGetCalendar generalGetCalendar = new GeneralGetCalendar();
        Set<Integer> years = new TreeSet<>();
        Map <Object, Integer> posts = new HashMap<>();
        SimpleDateFormat dateFormatForYear = new SimpleDateFormat(dateFormatYear);
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatCalendar);
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

    public ResponseEntity <ResponseApi> addComment (HttpServletRequest request, Integer parentId, int postId, String text) {
        GeneralPostComment generalPostComment = new GeneralPostComment();
        Map<String, String> errors = new HashMap<>();
        Integer userId = sessionInformation.getIdUserLogin(request);
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

    public ResponseEntity <ResponseApi> generalModeration (HttpServletRequest request, GeneralPostModerationObject information) {
        GeneralPostModeration generalPostModeration = new GeneralPostModeration();
        ModeratorStatus status;
        //Получаем id текущего пользователя
        Integer userId = sessionInformation.getIdUserLogin(request);
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
        Random random = new Random();//Случайности для генерации имен подпапок
        Integer userId = sessionInformation.getIdUserLogin(request);//Получаем id текущего пользователя
        //Проверяем, что пользователь авторизован
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        String userName = usersRepository.findById(userId).get().getName(); //Получаем имя пользоателя, использовал для создания имен подпапок
        StringBuilder pathToFolderWithImage = new StringBuilder();
        StringBuilder pathToImage = new StringBuilder();
        pathToFolderWithImage.append(pathToImages);
        //Создаем путь к конечной подпапке
        StringBuilder pathToImageForResponse = new StringBuilder();
        pathToImageForResponse.append(pathToImages);
        for (int i = 0; i < maxLevelOfDirectory; i++) {
            char pathPart = userName.charAt(random.nextInt(userName.length()));
            pathToFolderWithImage.append(pathPart);
            pathToFolderWithImage.append(File.separator);
            pathToImageForResponse.append(pathPart).append("-");
        }
        File newFolder = new File(pathToFolderWithImage.toString());
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        pathToImageForResponse.append(image.getOriginalFilename());
        pathToImage.append(pathToFolderWithImage).append(image.getOriginalFilename());
        BufferedImage bi = null;
        //Сохраняем картинку в нашу подпапку
        try {
            bi = ImageIO.read(image.getInputStream()); //Читаю файл с картинкой
            ImageIO.write(bi, "jpg", new File(String.valueOf(pathToImage))); //Записываю картинку в нашу подпапку с форматом png
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathToImageForResponse.toString().replaceAll(pathToImages, "api/");
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> generalMyStatistic (HttpServletRequest request) {
        GeneralGetMyStatistic generalGetMyStatistic = new GeneralGetMyStatistic();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost); //Перенести в настройки, взять от туда
        Integer userId = sessionInformation.getIdUserLogin(request);//Получаем id пользователя
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

    public ResponseEntity <ResponseApi> generalAllStatistic (HttpServletRequest request) {
        GeneralGetAllStatistic generalGetAllStatistic = new GeneralGetAllStatistic();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPost);
        Integer userId;
        userId = sessionInformation.getIdUserLogin(request);
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

    public ResponseEntity <ResponseApi> generalGetSetting (HttpServletRequest request) {
        GeneralGetSetting generalGetSetting = new GeneralGetSetting();
        Integer userId = sessionInformation.getIdUserLogin(request);
        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                generalGetSetting.setMULTIUSER_MODE(globalSettingRepository.findById(idMultiUserMode).get().getValue());
                generalGetSetting.setPOST_PREMODERATION(globalSettingRepository.findById(idPostPreModeration).get().getValue());
                generalGetSetting.setSTATISTICS_IS_PUBLIC(globalSettingRepository.findById(idStatisticsIsPublic).get().getValue());
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalGetSetting);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> generalPutSetting (HttpServletRequest request) {
        GeneralPutSetting generalPutSetting = new GeneralPutSetting();
        Integer userId = sessionInformation.getIdUserLogin(request);
        //Проверка, что пользователь авторизован
        if (!(userId == null)) {
            if (usersRepository.findById(userId).get().isModerator()) {
                generalPutSetting.setMULTIUSER_MODE(globalSettingRepository.findById(idMultiUserMode).get().getValue());
                generalPutSetting.setPOST_PREMODERATION(globalSettingRepository.findById(idPostPreModeration).get().getValue());
                generalPutSetting.setSTATISTICS_IS_PUBLIC(globalSettingRepository.findById(idStatisticsIsPublic).get().getValue());
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
       return ResponseEntity.status(HttpStatus.OK).body(generalPutSetting);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> generalMyProfileWithoutAvatar (HttpServletRequest request, GeneralPostMProfileObject information) {
        GeneralPostMyProfile generalPostMyProfile = new GeneralPostMyProfile();
        Integer userId = sessionInformation.getIdUserLogin(request);//Убрать зависимость с контроллером
        //Проверка авторизации пользователя
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            //Сохранение изменений профиля без аватарки
            boolean removePhoto = information.isRemovePhoto();
            String name = information.getName();
            String eMail = information.geteMail();
            String password = information.getPassword();
                Users user = usersRepository.findById(userId).get();
                if (removePhoto) {//Проверка необходимости удалить фотографию
                    user.setPhoto(null);
                }
                //Сохранение информации о пользователе
                user.setName(name);
                user.seteMail(eMail);
                if (password == null) {
                    password = user.getPassword();
                }
                else {
                    user.setPassword(password);
                }
                usersRepository.save(user);
                generalPostMyProfile.setResult(true);
            }
        return ResponseEntity.status(HttpStatus.OK).body(generalPostMyProfile);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity <ResponseApi> generalMyProfileWithAvatar (HttpServletRequest request, MultipartFile avatarFile, String removePhoto,
                                                      String name, String email, String password) {
        GeneralPostMyProfile generalPostMyProfile = new GeneralPostMyProfile();
        Integer userId = sessionInformation.getIdUserLogin(request);//Убрать взаимосвязь с контроллером
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        else {
            Users user = usersRepository.findById(userId).get();
            boolean needRemovePhoto = Boolean.parseBoolean(removePhoto);
            if (needRemovePhoto) {//Проверка необходимости удалить фотографию
                user.setPhoto(null);
            }
            else {
                user.setPhoto(avatarFile.getOriginalFilename());
                //Копируем аватарку в БД
                StringBuilder pathToFolderWithImage = new StringBuilder();
                StringBuilder pathToImage = new StringBuilder();
                pathToFolderWithImage.append(pathToAvatars);
                //Создадим дочернюю подпапку из индекса Юзера, т.к. он уникален
                pathToFolderWithImage.append(user.getId() + File.separator);
                File newFolder = new File(pathToFolderWithImage.toString());
                if (!newFolder.exists()) {
                    System.out.println("Создаем папку");
                    newFolder.mkdirs();
                }
                pathToImage.append(pathToFolderWithImage).append(avatarFile.getOriginalFilename());
                BufferedImage bi;
                try {
                    bi = ImageIO.read(avatarFile.getInputStream()); //Читаю файл с картинкой
                    ImageIO.write(bi, "jpg", new File(String.valueOf(pathToImage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Сохранение информации о пользователе
            user.setName(name);
            user.seteMail(email);
            if (password == null) {
                password = user.getPassword();
            }
            else {
                user.setPassword(password);
            }
            usersRepository.save(user);
            generalPostMyProfile.setResult(true);
        }
        return ResponseEntity.status(HttpStatus.OK).body(generalPostMyProfile);
    }
//---------------------------------------------------------------------------------------------------------------------

    public ResponseEntity<byte[]> getUserAvatar (HttpServletRequest request, String avatarImage) throws IOException {
        Integer userId = sessionInformation.getIdUserLogin(request);
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        File file;
        if (!avatarImage.equals("null")){
            file = new File(pathToAvatars + userId + File.separator + avatarImage);
        }
        else {
            file = new File(pathToDefaultAvatar);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        byte[] med = Files.readAllBytes(file.toPath());
        return new ResponseEntity<>(med, headers, HttpStatus.OK);
    }
}
