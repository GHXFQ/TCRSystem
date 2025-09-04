package com.tcr.system.controller;

import com.tcr.system.common.Result;
import com.tcr.system.dto.LoginDTO;
import com.tcr.system.dto.RegisterDTO;
import com.tcr.system.dto.UserInfoDTO;
import com.tcr.system.entity.User;
import com.tcr.system.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "用户管理")
public class UserController {

    private final UserService userService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Boolean> register(@Validated @RequestBody RegisterDTO registerDTO) {
        System.out.println("接收到注册请求: " + registerDTO.getUsername());
        boolean result = userService.register(registerDTO);
        System.out.println("注册结果: " + result);
        return Result.success(result, "注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserInfoDTO> login(@Validated @RequestBody LoginDTO loginDTO) {
        System.out.println("接收到登录请求: " + loginDTO.getUsername());
        UserInfoDTO userInfoDTO = userService.login(loginDTO);
        System.out.println("登录成功，生成的 token: " + userInfoDTO.getToken());
        return Result.success(userInfoDTO, "登录成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前用户信息")
    public Result<UserInfoDTO> info(@RequestHeader("Authorization") String token) {
        // 这里简单处理，实际项目中应该从token中获取用户信息
        // 这部分逻辑应该在拦截器或过滤器中处理
        return Result.success();
    }
    
    /**
     * 获取用户个人资料
     */
    @GetMapping("/profile")
    @ApiOperation("获取用户个人资料")
    public Result<UserInfoDTO> getProfile(@RequestHeader("Authorization") String token) {
        // 修复token处理
        if (token == null || token.isEmpty()) {
            return Result.fail("token不能为空");
        }
        
        Long userId = userService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail("无效的token");
        }
        
        // 先获取基本用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(user.getId());
        userInfoDTO.setUsername(user.getUsername());
        userInfoDTO.setRealName(user.getRealName());
        userInfoDTO.setEmail(user.getEmail());
        userInfoDTO.setRole(user.getRole());
        userInfoDTO.setAvatar(user.getAvatar());
        
        // 根据角色获取详细信息
        if (user.getRole() == 0) { // 学生
            // 获取学生详细信息
            User studentDetail = userService.getStudentDetailById(userId);
            if (studentDetail != null) {
                userInfoDTO.setStudentId(studentDetail.getUsername());
                userInfoDTO.setClassId(studentDetail.getClassId());
                userInfoDTO.setClassName(studentDetail.getClassName());
                userInfoDTO.setMajorName(studentDetail.getMajorName());
                userInfoDTO.setCollegeName(studentDetail.getCollegeName());
            }
        } else if (user.getRole() == 1) { // 教师
            // 获取教师详细信息
            User teacherDetail = userService.getTeacherDetailById(userId);
            if (teacherDetail != null) {
                userInfoDTO.setTeacherId(teacherDetail.getUsername());
                userInfoDTO.setCollegeName(teacherDetail.getCollegeName());
            }
        }
        
        return Result.success(userInfoDTO, "获取个人资料成功");
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @ApiOperation("修改密码")
    public Result<Boolean> changePassword(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, String> passwordMap) {
        // 修复token处理
        if (token == null || token.isEmpty()) {
            return Result.fail("token不能为空");
        }
        
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.fail("密码不能为空");
        }
        
        Long userId = userService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail("无效的token");
        }
        
        try {
            boolean result = userService.changePassword(userId, oldPassword, newPassword);
            return result ? Result.success(true, "密码修改成功") : Result.fail("原密码错误");
        } catch (Exception e) {
            return Result.fail("密码修改失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    @ApiOperation("上传头像")
    public Result<String> uploadAvatar(@RequestHeader("Authorization") String token,
                                      @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        
        Long userId = userService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail("无效的token");
        }
        
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 生成新的文件名
        String newFileName = "avatar_" + userId + "_" + UUID.randomUUID().toString().replaceAll("-", "") + suffix;
        
        // 创建头像目录
        String avatarDir = uploadDir + "/avatars";
        File dir = new File(avatarDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 保存文件
        try {
            File dest = new File(avatarDir + "/" + newFileName);
            file.transferTo(dest);
            
            // 更新用户头像路径 - 修复URL问题
            String avatarUrl = "/static/avatars/" + newFileName;
            User user = userService.getById(userId);
            user.setAvatar(avatarUrl);
            userService.updateById(user);
            
            return Result.success(avatarUrl, "头像上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("用户退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // 清除token
        userService.logout(token);
        return Result.success(null, "退出成功");
    }
} 