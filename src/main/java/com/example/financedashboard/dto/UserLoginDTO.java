package com.example.financedashboard.dto;

import lombok.Data;

/**
 * 用户登录数据传输对象
 * 用于封装前端传递到后端的用户登录信息
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class UserLoginDTO {
    private String username;  // 用户名，用户登录的唯一标识
    private String password;  // 密码，用户登录的凭证
}