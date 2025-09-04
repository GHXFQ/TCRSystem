package com.tcr.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogInterceptor());
    }

    private static class RequestLogInterceptor extends HandlerInterceptorAdapter {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            System.out.println("==================================================");
            System.out.println("请求详情:");
            System.out.println("方法: " + request.getMethod());
            System.out.println("路径: " + request.getRequestURI());
            System.out.println("完整URL: " + request.getRequestURL());
            System.out.println("查询字符串: " + request.getQueryString());
            System.out.println("内容类型: " + request.getContentType());
            
            System.out.println("请求参数:");
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                System.out.println(paramName + " = " + request.getParameter(paramName));
            }
            
            System.out.println("请求头:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("authorization")) { // 不打印敏感信息
                    System.out.println(headerName + " = " + request.getHeader(headerName));
                } else {
                    System.out.println(headerName + " = [已隐藏]");
                }
            }
            System.out.println("==================================================");
            return true;
        }
    }
} 