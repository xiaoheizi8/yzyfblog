package cn.yzfy.blog.util;

import com.google.gson.Gson;

/**
 * @author 一朝风月
 * @description 简单的 JSON 工具，封装 Gson 以便复用。
 * @datetime 2026-03-19
 */
public final class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}

