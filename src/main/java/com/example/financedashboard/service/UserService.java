package com.example.financedashboard.service;

import com.example.financedashboard.dto.UserRegisterDTO;
import com.example.financedashboard.dto.UserLoginDTO;
import com.example.financedashboard.dto.UserUpdateDTO;
import com.example.financedashboard.dto.ChangePasswordDTO;
import com.example.financedashboard.vo.UserVO;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    String login(UserLoginDTO userLoginDTO);
    UserVO getUserInfo(String token);
    boolean logout(String token);
    boolean updateUserInfo(String token, UserUpdateDTO userUpdateDTO);
    boolean changePassword(String token, ChangePasswordDTO changePasswordDTO);
}
