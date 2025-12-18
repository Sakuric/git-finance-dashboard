package com.example.financedashboard.service.impl;

import com.example.financedashboard.dto.UserRegisterDTO;
import com.example.financedashboard.dto.UserLoginDTO;
import com.example.financedashboard.dto.UserUpdateDTO;
import com.example.financedashboard.dto.ChangePasswordDTO;
import com.example.financedashboard.entity.User;
import com.example.financedashboard.mapper.UserMapper;
import com.example.financedashboard.service.UserService;
import com.example.financedashboard.utils.JwtUtil;
import com.example.financedashboard.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean register(UserRegisterDTO userRegisterDTO) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(userRegisterDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.findByEmail(userRegisterDTO.getEmail()) != null) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) {
        User user = userMapper.findByUsername(userLoginDTO.getUsername());
        if (user == null || !passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 生成JWT令牌
        return jwtUtil.generateToken(user);
    }

    @Override
    public UserVO getUserInfo(String token) {
        // 从token中获取用户信息
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return true;
    }

    @Override
    public boolean updateUserInfo(String token, UserUpdateDTO userUpdateDTO) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getUsername() != null) {
            user.setUsername(userUpdateDTO.getUsername());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean changePassword(String token, ChangePasswordDTO changePasswordDTO) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.update(user) > 0;
    }
}
