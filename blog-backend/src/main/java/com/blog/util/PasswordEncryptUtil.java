package com.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密测试工具类
 *
 * @author 一朝风月
 */
public class PasswordEncryptUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 控制台测试入口：
     * 直接运行本 main 方法，在运行配置里传入明文密码参数，
     * 不传参数时默认使用 admin123。
     *
     * 示例：
     *  - 无参数：加密 admin123
     *  - 参数：123456  -> 加密 123456
     */
    public static void main(String[] args) {
        String raw = (args != null && args.length > 0) ? args[0] : "admin123";
        String encoded = encode("123456");
        System.out.println("Raw: " + 123456);
        System.out.println("BCrypt: " + encoded);
    }

    /**
     * 加密明文密码
     *
     * @param raw 明文
     * @return BCrypt 密文
     */
    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    /**
     * 校验明文与密文是否匹配
     *
     * @param raw     明文
     * @param encoded 密文
     * @return 是否匹配
     */
    public static boolean matches(String raw, String encoded) {
        return ENCODER.matches(raw, encoded);
    }
}

