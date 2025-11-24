package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息实体类
 * 对应数据库表: user_info
 * 用于存储用户的基本信息和账号状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;                      // 用户ID，主键
    private String username;              // 用户名，唯一索引
    private String password;              // 密码（BCrypt加密）
    private String email;                 // 邮箱，唯一索引
    private String phoneNumber;           // 手机号
    private String avatarUrl;             // 头像URL
    private Integer status;               // 状态：0-禁用，1-正常
    private LocalDateTime createdAt;      // 创建时间
    private LocalDateTime updatedAt;      // 更新时间
}
