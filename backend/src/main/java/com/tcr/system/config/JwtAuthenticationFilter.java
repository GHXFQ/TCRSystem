package com.tcr.system.config;

import com.tcr.system.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 定义不需要token的公开接口
    private final List<String> publicUrls = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/user/logout",
            "/api/file/**",
            "/api/course/list",
            "/doc.html",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("JWT过滤器处理请求: " + method + " " + requestURI);

        // 检查是否是公开接口
        if (isPublicUrl(requestURI)) {
            System.out.println("公开接口，直接放行: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = request.getHeader("Authorization");
        System.out.println("获取到的token: " + token);
        
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("处理后的token: " + token);
            
            try {
                // 验证token
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("从token中获取的用户名: " + username);
                
                if (username != null) {
                    // 获取用户ID和角色
                    Object userIdObj = jwtUtil.getClaimFromToken(token, claims -> claims.get("userId"));
                    Object roleObj = jwtUtil.getClaimFromToken(token, claims -> claims.get("role"));
                    
                    Long userId = null;
                    if (userIdObj != null) {
                        if (userIdObj instanceof Integer) {
                            userId = ((Integer) userIdObj).longValue();
                        } else if (userIdObj instanceof Long) {
                            userId = (Long) userIdObj;
                        } else {
                            userId = Long.parseLong(userIdObj.toString());
                        }
                    }
                    
                    String role = roleObj != null ? roleObj.toString() : "";
                    
                    System.out.println("从token中获取的用户ID: " + userId);
                    System.out.println("从token中获取的角色: " + role);
                    
                    String roleName = getRoleName(role);
                    System.out.println("转换后的角色名: ROLE_" + roleName);
                    
                    // 创建认证信息
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
                    );
                    
                    // 设置认证信息到上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 将用户信息存入请求属性
                    request.setAttribute("username", username);
                    request.setAttribute("userId", userId);
                    request.setAttribute("role", role);
                    
                    System.out.println("认证信息已设置到SecurityContext和Request属性");
                }
            } catch (Exception e) {
                System.out.println("JWT认证失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("未找到token或token格式不正确");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicUrl(String requestUri) {
        return publicUrls.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
    
    private String getRoleName(String role) {
        switch (role) {
            case "0":
                return "STUDENT";
            case "1":
                return "TEACHER";
            case "2":
                return "ADMIN";
            default:
                return "USER";
        }
    }
} 