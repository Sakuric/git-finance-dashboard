package com.example.financedashboard.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
/**
 * 角色实体类
 * 用于存储角色信息，实现基于角色的访问控制(RBAC)
 * Role（角色）是指用户在系统中的身份或职责，如管理员、普通用户等
 * 每个角色拥有一组特定的权限，用户通过被分配角色来获得相应的权限
 */

