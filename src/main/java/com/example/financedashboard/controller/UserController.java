package com.example.financedashboard.controller;

import com.example.financedashboard.dto.UserLoginDTO;
import com.example.financedashboard.dto.UserRegisterDTO;
import com.example.financedashboard.dto.UserUpdateDTO;
import com.example.financedashboard.dto.ChangePasswordDTO;
import com.example.financedashboard.service.UserService;
import com.example.financedashboard.utils.Result;
import com.example.financedashboard.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        boolean success = userService.register(userRegisterDTO);
        return success ? Result.success(true) : Result.error(500, "用户注册失败");
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        String token = userService.login(userLoginDTO);
        return token != null ? Result.success(token) : Result.error(500, "用户登录失败");
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        // 处理Bearer token格式
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserVO userVO = userService.getUserInfo(token);
        return userVO != null ? Result.success(userVO) : Result.error(500, "获取用户信息失败");
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean success = userService.logout(token);
        return success ? Result.success(true) : Result.error(500, "用户登出失败");
    }

    @PutMapping("/update")
    public Result<Boolean> updateUserInfo(HttpServletRequest request, @RequestBody UserUpdateDTO userUpdateDTO) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean success = userService.updateUserInfo(token, userUpdateDTO);
        return success ? Result.success(true) : Result.error(500, "用户信息更新失败");
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(HttpServletRequest request, @RequestBody ChangePasswordDTO changePasswordDTO) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean success = userService.changePassword(token, changePasswordDTO);
        return success ? Result.success(true) : Result.error(500, "密码修改失败");
    }
}
