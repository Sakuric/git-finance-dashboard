package com.example.financedashboard.interceptor;

import com.example.financedashboard.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器 - 已弃用
 * 注意：此拦截器已被Spring Security的JWT过滤器替代，不再使用
 * JWT认证现在由SecurityConfig中的JwtAuthenticationFilter处理
 * 
 * @deprecated 使用Spring Security的JWT认证机制
 */
@Deprecated
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 此方法不再被调用，认证由Spring Security处理
        return true;
    }
}
