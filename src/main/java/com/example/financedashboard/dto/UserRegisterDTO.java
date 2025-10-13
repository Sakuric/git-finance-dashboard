package com.example.financedashboard.dto;

import lombok.Data;

/**
 * 用户注册数据传输对象
 * 用于封装前端传递到后端的用户注册信息
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class UserRegisterDTO {
    private String username;  // 用户名，用户登录的唯一标识
    private String password;  // 密码，用户登录的凭证
    private String email;     // 邮箱，可用于找回密码和接收通知
    private String phone;     // 手机号，可用于验证和接收通知
}