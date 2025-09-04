package com.tcr.system.service;

import com.tcr.system.dto.LoginDTO;
import com.tcr.system.dto.RegisterDTO;
import com.tcr.system.dto.UserInfoDTO;
import com.tcr.system.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    boolean register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 用户信息
     */
    UserInfoDTO login(LoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateById(User user);

    /**
     * 用户退出登录
     *
     * @param token 用户token
     */
    void logout(String token);

    /**
     * 获取教师列表
     *
     * @return 教师列表
     */
    java.util.List<User> getTeacherList();

    /**
     * 从token中获取用户ID
     *
     * @param token 用户token
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取学生详细信息（包括班级、专业和学院信息）
     *
     * @param userId 用户ID
     * @return 学生详细信息
     */
    User getStudentDetailById(Long userId);

    /**
     * 获取教师详细信息（包括学院信息）
     *
     * @param userId 用户ID
     * @return 教师详细信息
     */
    User getTeacherDetailById(Long userId);
} 