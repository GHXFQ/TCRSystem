package com.tcr.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名/学号/工号
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色：0-学生，1-教师，2-管理员
     */
    private Integer role;

    /**
     * token
     */
    private String token;
    
    /**
     * 学生学号（仅学生用户）
     */
    private String studentId;
    
    /**
     * 教师工号（仅教师用户）
     */
    private String teacherId;
    
    /**
     * 班级ID（仅学生用户）
     */
    private Long classId;
    
    /**
     * 班级名称（仅学生用户）
     */
    private String className;
    
    /**
     * 专业名称（仅学生用户）
     */
    private String majorName;
    
    /**
     * 学院名称
     */
    private String collegeName;
} 