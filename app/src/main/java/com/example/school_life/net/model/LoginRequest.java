package com.example.school_life.net.model;

/** 登录请求体 */
public class LoginRequest {
    public final String studentNo;
    public final String password;

    public LoginRequest(String studentNo, String password) {
        this.studentNo = studentNo;
        this.password = password;
    }
}
