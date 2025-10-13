package com.example.financedashboard.service;

import com.example.financedashboard.dto.UserRegisterDTO;
import com.example.financedashboard.dto.UserLoginDTO;
import com.example.financedashboard.vo.UserVO;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    String login(UserLoginDTO userLoginDTO);
    UserVO getUserInfo(String token);
    boolean logout(String token);
}
