package com.tcr.system.config;

import com.tcr.system.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 角色拦截器
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("角色拦截器处理请求: " + method + " " + requestURI);

        // 对于OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("OPTIONS请求，直接放行");
            return true;
        }

        // 获取用户角色
        Object role = request.getAttribute("role");
        System.out.println("当前用户角色: " + role);

        // 检查URL路径
        if (requestURI.startsWith("/api/student/")) {
            // 学生接口只允许学生访问
            if (role == null || !role.toString().equals("0")) {
                System.out.println("非学生用户尝试访问学生接口，拒绝访问");
                throw new BusinessException("无权访问");
            }
        } else if (requestURI.startsWith("/api/teacher/")) {
            // 教师接口只允许教师访问
            if (role == null || !role.toString().equals("1")) {
                System.out.println("非教师用户尝试访问教师接口，拒绝访问");
                throw new BusinessException("无权访问");
            }
        } else if (requestURI.startsWith("/api/admin/")) {
            // 管理员接口只允许管理员访问
            if (role == null || !role.toString().equals("2")) {
                System.out.println("非管理员用户尝试访问管理员接口，拒绝访问");
                throw new BusinessException("无权访问");
            }
        }

        System.out.println("角色验证通过");
        return true;
    }
} 