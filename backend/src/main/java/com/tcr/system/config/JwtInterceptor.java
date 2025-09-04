package com.tcr.system.config;

import com.tcr.system.exception.BusinessException;
import com.tcr.system.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("JWT拦截器处理请求: " + method + " " + requestURI);
        
        // 对于OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("OPTIONS请求，直接放行");
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        System.out.println("获取到的 token: " + token);
        
        // 如果没有token，抛出异常
        if (!StringUtils.hasText(token)) {
            System.out.println("token 为空，拒绝请求: " + requestURI);
            throw new BusinessException("未登录或登录已过期");
        }
        
        // 处理可能的 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            System.out.println("token 包含 Bearer 前缀，移除前缀");
            token = token.substring(7);
        }
        
        try {
            // 打印token的每个部分，用于调试
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                System.out.println("Token 头部: " + parts[0]);
                System.out.println("Token 载荷: " + parts[1]);
                System.out.println("Token 签名: " + parts[2]);
            } else {
                System.out.println("Token 格式不正确，无法分割为三部分");
            }
            
            // 验证token
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                System.out.println("从 token 中获取的用户名为空，拒绝请求: " + requestURI);
                throw new BusinessException("未登录或登录已过期");
            }
            
            System.out.println("从 token 中获取的用户名: " + username);
            
            // 获取用户ID和角色
            Object userId = jwtUtil.getClaimFromToken(token, claims -> claims.get("userId"));
            Object role = jwtUtil.getClaimFromToken(token, claims -> claims.get("role"));
            
            System.out.println("从 token 中获取的用户ID: " + userId);
            System.out.println("从 token 中获取的角色: " + role);
            
            // 将用户信息存入请求属性
            request.setAttribute("username", username);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            
            System.out.println("JWT 验证成功，允许请求: " + requestURI);
            return true;
        } catch (Exception e) {
            System.out.println("验证 token 时发生异常: " + e.getMessage());
            e.printStackTrace();
            System.out.println("JWT 验证失败，拒绝请求: " + requestURI);
            throw new BusinessException("未登录或登录已过期");
        }
    }
} 