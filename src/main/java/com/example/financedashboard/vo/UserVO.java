package com.example.financedashboard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 * VO（View Object）是一种设计模式，用于封装展示层需要的数据
 * UserVO用于封装后端传递到前端的用户信息，不包含敏感信息如密码
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class UserVO {
    private Long id;              // 用户ID，主键
    private String username;      // 用户名，用户登录的唯一标识
    private String email;         // 邮箱，可用于找回密码和接收通知
    private String phone;         // 手机号，可用于验证和接收通知
    private Integer status;       // 用户状态，0-禁用，1-启用
    private LocalDateTime createTime;  // 创建时间
}