package cn.yzfy.blog.data.net;

import android.content.Context;

import androidx.annotation.NonNull;

import cn.yzfy.blog.data.SessionManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.UUID;

/**
 * @author 一朝风月
 * @description Retrofit/OkHttp 客户端工厂：统一 baseUrl、日志与 token 注入。
 * @datetime 2026-03-18
 */
public final class ApiClient {
    private static volatile Retrofit retrofit;

    private ApiClient() {}

    /**
     * 获取 Retrofit 实例（全局单例）。
     *
     * @param context 上下文（用于读取 token）
     * @param baseUrl 后端基础地址（必须以 / 结尾）
     */
    public static Retrofit get(@NonNull Context context, @NonNull String baseUrl) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
                    logger.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        String token = SessionManager.getToken(context);
                        if (token == null || token.isEmpty()) {
                            return chain.proceed(original);
                        }
                        Request withAuth = original.newBuilder()
                                .header("Authorization", token)
                                .build();
                        return chain.proceed(withAuth);
                    };

                    // 为写操作自动附加幂等 Key（对齐 uniapp 的行为）
                    Interceptor idempotencyInterceptor = chain -> {
                        Request original = chain.request();
                        String method = original.method();
                        if (method != null) {
                            String m = method.toUpperCase();
                            if ("POST".equals(m) || "PUT".equals(m) || "DELETE".equals(m)) {
                                String idemKey = Long.toString(System.currentTimeMillis(), 36)
                                        + "-" + UUID.randomUUID().toString().replace("-", "");
                                Request withIdem = original.newBuilder()
                                        .header("Idempotency-Key", idemKey)
                                        .build();
                                return chain.proceed(withIdem);
                            }
                        }
                        return chain.proceed(original);
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(idempotencyInterceptor)
                            .addInterceptor(logger)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}

