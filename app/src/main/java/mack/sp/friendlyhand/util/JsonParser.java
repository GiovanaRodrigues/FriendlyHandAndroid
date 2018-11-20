package mack.sp.friendlyhand.util;

import com.google.gson.Gson;


import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public class JsonParser<T> {
    final Class<T> tipoClasse;
    Gson gson = new Gson();

    public JsonParser(Class<T> tipoClasse) {
        this.tipoClasse = tipoClasse;
    }

    public T toObject(String json) {
        return gson.fromJson(json, tipoClasse);
    }

    public List<T> toList(String json, Class<T[]> classe) {
        return Arrays.asList(gson.fromJson(json, classe));
    }

    public String fromObject(T object) {
        return gson.toJson(object);
    }


}
