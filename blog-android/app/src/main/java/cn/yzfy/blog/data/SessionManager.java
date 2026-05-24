package cn.yzfy.blog.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * @author 一朝风月
 * @description 本地会话管理（token、用户信息缓存）。
 * @datetime 2026-03-18
 */
public final class SessionManager {
    private static final String PREFS_NAME = "blog_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_JSON = "user_json";

    private SessionManager() {}

    /**
     * 保存登录 token（Sa-Token，header: Authorization）。
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * 获取登录 token。
     */
    @Nullable
    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, null);
    }

    /**
     * 保存用户信息 JSON（用于简单展示与恢复登录态）。
     */
    public static void saveUserJson(Context context, String userJson) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_USER_JSON, userJson).apply();
    }

    /**
     * 获取用户信息 JSON。
     */
    @Nullable
    public static String getUserJson(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_USER_JSON, null);
    }

    /**
     * 清除会话（退出登录）。
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}

