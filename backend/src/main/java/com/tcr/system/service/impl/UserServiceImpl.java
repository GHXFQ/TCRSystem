package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.dto.LoginDTO;
import com.tcr.system.dto.RegisterDTO;
import com.tcr.system.dto.UserInfoDTO;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.UserService;
import com.tcr.system.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setRealName(registerDTO.getRealName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole());
        user.setStatus(1);

        return save(user);
    }

    @Override
    public UserInfoDTO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        
        // 如果用户不存在，抛出异常
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查密码是否正确
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }
        
        // 生成token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        System.out.println("生成 token: " + token);
        
        // 构建返回对象
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        userInfoDTO.setToken(token);
        
        return userInfoDTO;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public User getById(Long id) {
        return super.getById(id);
    }

    /**
     * 获取学生详细信息（包括班级、专业和学院信息）
     * @param userId 用户ID
     * @return 学生详细信息
     */
    @Override
    public User getStudentDetailById(Long userId) {
        return baseMapper.getStudentDetailById(userId);
    }
    
    /**
     * 获取教师详细信息（包括学院信息）
     * @param userId 用户ID
     * @return 教师详细信息
     */
    @Override
    public User getTeacherDetailById(Long userId) {
        return baseMapper.getTeacherDetailById(userId);
    }

    @Override
    public boolean updateById(User user) {
        return super.updateById(user);
    }

    @Override
    public void logout(String token) {
        // 从token中获取用户信息
        try {
            // 这里可以添加将token加入黑名单的逻辑
            // 如果使用Redis，可以将token加入Redis黑名单，并设置过期时间
            // 由于当前项目可能没有使用Redis，这里只做简单处理
            // 实际项目中应该根据具体的token管理方式来实现
            String username = jwtUtil.getUsernameFromToken(token);
            // 验证token是否有效
            if (username != null && jwtUtil.validateToken(token, username)) {
                // token有效，可以在这里将token加入黑名单
            }
        } catch (Exception e) {
            // token已经无效，不需要处理
        }
    }

    @Override
    public List<User> getTeacherList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 1);
        return list(wrapper);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // 如果token以"Bearer "开头，截取后面的部分
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            // 从token中获取用户ID
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            // token 解析失败
            System.out.println("解析Token出错: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 获取用户信息
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码 - 此处修改为直接比较，因为之前的实现可能存在问题
        // 如果数据库中的密码没有加密，应该直接比较
        if (!user.getPassword().equals(oldPassword)) {
            return false;
        }
        
        // 更新密码 - 根据实际情况决定是否加密
        // 如果数据库中的密码没有加密，这里也不应该加密
        user.setPassword(newPassword);
        return updateById(user);
    }
} 