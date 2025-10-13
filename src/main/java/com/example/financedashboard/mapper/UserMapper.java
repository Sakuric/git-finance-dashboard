package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    User findById(@Param("id") Long id);
    int update(User user);
}
