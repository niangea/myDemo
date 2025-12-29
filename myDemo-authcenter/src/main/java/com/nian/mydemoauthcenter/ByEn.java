package com.nian.mydemoauthcenter;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ByEn {
    public static void main(String[] args) {
        // 创建 BCryptPasswordEncoder 实例
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 要加密的密码
        String rawPassword = "test";

        // 加密密码
        String encodedPassword = encoder.encode(rawPassword);

        // 输出加密结果
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);

        // 验证密码（可选，用于确认加密正确）
        boolean isMatch = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + isMatch);
    }
}