package main.service.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.models.PostsRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostGetPost {

    @JsonProperty
    int count;

    @JsonProperty
    ArrayList <Map> posts = new ArrayList<>();

    public PostGetPost (int offset, int limit, String mode, PostsRepository postsRepository) {

        ArrayList<Map> allPosts = new ArrayList<>();
        Map <Object, Object> answerJson = new HashMap<Object, Object>();

        //Выбор метода отображения списка постов
        switch (mode) {
            case ("recent"):
                //Создаем список id постов с датами публикации
                Map<Integer, Date> idPostsListRecently = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListRecently.put(post.getId(), post.getTime());
                    }
                });
                //answerJson.put("count", idPostsListRecently.size());
                count = idPostsListRecently.size();// Фиксируем количество постов
                idPostsListRecently.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue().reversed())//Выполняем сортировку по датам
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));//в порядке очереди заполняем информацию о постах

                break;
            case ("popular"):
                //Создаем список id постов с количеством коментариев
                Map<Integer, Integer> idPostsListPopular = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListPopular.put(post.getId(), post.getCommentsToPost().size());
                    }
                });
                answerJson.put("count", idPostsListPopular.size());//Фиксируем количество постов
                idPostsListPopular.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())//Выполняем сортировку по количеству коментариев
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));//в порядке очереди заполняем информацию о постах
                break;
            case ("best"):
                //Создаем список id постов с количеством лайков
                Map<Integer, Integer> idPostsListBest = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {

                        //Считаем количество лайков для постов
                        int likeCount = 0;
                        for (int i = 0; i < post.getVotesToPost().size(); i++) {
                            if (post.getVotesToPost().get(i).isValue()) {
                                likeCount++;
                            }
                            idPostsListBest.put(post.getId(), likeCount);//Заполняем список
                        }
                    }
                });
                answerJson.put("count", idPostsListBest.size());//Фиксируем количество постов
                idPostsListBest.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue())//Выполняем сортировку
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));//Заполняем в порядке очереди информацию о остах
                break;
            case ("early"):
                //Создаем список id постов с датами
                Map<Integer, Date> idPostsListEarly = new HashMap<>();
                postsRepository.findAll().forEach(post -> {
                    if (post.isActive() && post.getModerationStatus().toString().equals("ACCEPTED")&& post.getTime().before(new Date())) {
                        idPostsListEarly.put(post.getId(), post.getTime());
                    }
                });
                answerJson.put("count", idPostsListEarly.size());//Фиксируем количество постов
                idPostsListEarly.entrySet().stream().sorted(Map.Entry.<Integer, Date>comparingByValue())//Сортируем по датам
                        .forEach(postId -> allPosts
                                .add(getPostInformation(postId.getKey(), postsRepository)));//Заполняем по очереди информацию о постах
                break;
            default:
                answerJson.put("count", 0);
                break;
        }

        //Определяемся с количеством выводимого на экран
        //Если больше постов больше, чем offset, то количество равно offset, если меньше, то все
        if (allPosts.size() >= (offset + limit)) {
            for (int i = offset; i < limit; i++) {
                posts.add(allPosts.get(i));
            }
        }
        else {
            for (int i = offset; i < allPosts.size(); i++) {
                posts.add(allPosts.get(i));
            }
        }
    }

    //Метод формирования информации о постах
    public static Map <Object, Object> getPostInformation (Integer postId, PostsRepository postsRepository) {
        int annoncelength = 100;//Количество знаков в анонсе

        Map <Object, Object> mapForAnswer = new HashMap<>();
        Map <Object, Object> userMap = new HashMap<>();

        //Заполнение информации
        mapForAnswer.put("id", postId);
        mapForAnswer.put("time", postsRepository.findById(postId).get().getTime().toString());
        userMap.put("id", postsRepository.findById(postId).get().getUser().getId());
        userMap.put("name", postsRepository.findById(postId).get().getUser().getName());
        mapForAnswer.put("user", userMap);
        mapForAnswer.put("title", postsRepository.findById(postId).get().getTitle());

        String annonce;
        //Формирование анонса
        if (postsRepository.findById(postId).get().getText().length() < annoncelength) {//Проверка, что длина текста больше максимальной длины анонса
            annonce = postsRepository.findById(postId).get().getText();
        }
        else {
            annonce = postsRepository.findById(postId).get().getText().substring(0, annoncelength) + "...";//Добавление троеточия в конце анонса
        }

        mapForAnswer.put("announce", annonce);

        int likeCount = 0;
        int dislikeCounts = 0;
        //Подсчет количества лайков и дизлайков
        for (int i = 0; i < postsRepository.findById(postId).get().getVotesToPost().size(); i++) {
            if (postsRepository.findById(postId).get().getVotesToPost().get(i).isValue()){
                likeCount++;
            }
            else {
                dislikeCounts++;
            }
        }
        mapForAnswer.put("likeCount", likeCount);
        mapForAnswer.put("dislikeCount", dislikeCounts);
        mapForAnswer.put("commentCount", postsRepository.findById(postId).get().getCommentsToPost().size());
        mapForAnswer.put("viewCount", postsRepository.findById(postId).get().getViewCount());

        return mapForAnswer;
    }
}
