package com.example.financedashboard.config;

import com.example.financedashboard.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/api/test/**").permitAll() // 允许访问测试接口
                        .requestMatchers("/api/sina/sync/**").permitAll() // 允许访问新浪数据同步接口
                        .requestMatchers("/api/crawler/**").permitAll() // 允许访问股票信息爬虫接口
                        .requestMatchers("/api/history-crawler/**").permitAll() // 允许访问历史数据爬虫接口(已废弃)
                        .requestMatchers("/api/stock-data/**").permitAll() // 允许访问股票实时数据接口
                        .requestMatchers("/api/technical-indicators/**").permitAll() // 允许访问技术指标API接口
                        .requestMatchers("/api/llm/**").permitAll() // 允许访问大模型接口
                        .requestMatchers("/api/ai-models/**").permitAll() // 允许访问AI模型配置接口
                        .requestMatchers("/api/investment-preference/**").permitAll() // 允许访问投资偏好接口
                        .requestMatchers("/api/favorites/**").permitAll()//允许访问用户偏好接口
                        .requestMatchers("/api/advice/**").permitAll()//允许访问投资建议接口
                        .requestMatchers("/api/backtest/**").permitAll()//允许访问回测接口
                        .requestMatchers("/api/structured-advice/**").permitAll()//允许访问结构化投资建议接口
                        .requestMatchers("/api/**").permitAll()//允许访问接口
                        .requestMatchers("/error").permitAll() // 允许访问错误页面
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable()) // 禁用HTTP Basic认证
                .formLogin(formLogin -> formLogin.disable()) // 禁用表单登录
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 允许所有来源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的方法
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允许所有头部
        configuration.setAllowCredentials(true); // 允许携带凭证
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // JWT认证过滤器
    public class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;

        public JwtAuthenticationFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String token = request.getHeader("Authorization");
            
            // 处理Bearer token格式
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
        }
    }
}
