package main.controller;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ApiGeneralController {

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

    @GetMapping ("/api/tag")
    public static String getTag () {
        Map <String, String> mapstr = new HashMap<>();
        Map <String, Integer> mapint = new HashMap<>();
        Map <String, ArrayList> mapMap = new HashMap<>();
        ArrayList <Map> arrayMap = new ArrayList<>();
        mapstr.put("name", "Java");
        mapint.put("weight", 1);
        arrayMap.add(mapstr);
        arrayMap.add(mapint);
        mapMap.put("tags", arrayMap);
        return new Gson().toJson(mapMap);
    }



}
