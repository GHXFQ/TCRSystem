package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Feedback;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.FeedbackMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

/**
 * 反馈服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final UserMapper userMapper;

    @Override
    public Page<Feedback> page(Page<Feedback> page, Long courseId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        // 只查询父级反馈（不是回复的反馈）
        wrapper.isNull(Feedback::getParentId);
        
        // 如果指定了课程ID，则按课程ID查询
        if (courseId != null) {
            wrapper.eq(Feedback::getCourseId, courseId);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Feedback::getCreateTime);
        
        // 查询分页数据
        Page<Feedback> result = page(page, wrapper);
        
        // 收集所有用户ID
        List<Long> userIds = result.getRecords().stream()
            .map(Feedback::getUserId)
            .collect(Collectors.toList());
        
        log.debug("收集到的父级反馈用户ID: {}", userIds);
        
        // 查询每个反馈的回复
        result.getRecords().forEach(feedback -> {
            LambdaQueryWrapper<Feedback> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Feedback::getParentId, feedback.getId());
            replyWrapper.orderByAsc(Feedback::getCreateTime);
            List<Feedback> replies = list(replyWrapper);
            feedback.setChildren(replies);
            
            // 收集回复的用户ID
            List<Long> replyUserIds = replies.stream()
                .map(Feedback::getUserId)
                .collect(Collectors.toList());
            userIds.addAll(replyUserIds);
            
            log.debug("反馈ID: {}, 回复数量: {}, 回复用户ID: {}", feedback.getId(), replies.size(), replyUserIds);
        });
        
        log.info("需要查询的所有用户ID总数: {}, 用户ID列表: {}", userIds.size(), userIds);
        
        // 批量查询用户信息
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(User::getId, userIds);
            List<User> users = userMapper.selectList(userWrapper);
            
            log.info("查询到的用户数量: {}", users.size());
            
            // 创建用户ID到用户信息的映射
            Map<Long, User> userMap = new HashMap<>();
            for (User user : users) {
                userMap.put(user.getId(), user);
                log.debug("用户ID: {}, 真实姓名: {}, 用户名: {}", user.getId(), user.getRealName(), user.getUsername());
            }
            
            // 填充反馈的用户信息
            for (Feedback feedback : result.getRecords()) {
                User user = userMap.get(feedback.getUserId());
                if (user != null) {
                    String userName = user.getRealName() != null && !user.getRealName().trim().isEmpty() 
                            ? user.getRealName() : user.getUsername();
                    feedback.setUserName(userName);
                    feedback.setUserAvatar(user.getAvatar());
                    log.debug("设置反馈用户名 - 反馈ID: {}, 用户ID: {}, 用户名: {}", feedback.getId(), feedback.getUserId(), userName);
                } else {
                    log.warn("未找到反馈用户 - 反馈ID: {}, 用户ID: {}", feedback.getId(), feedback.getUserId());
                }
                
                // 填充回复的用户信息
                if (feedback.getChildren() != null) {
                    for (Feedback reply : feedback.getChildren()) {
                        User replyUser = userMap.get(reply.getUserId());
                        if (replyUser != null) {
                            String replyUserName = replyUser.getRealName() != null && !replyUser.getRealName().trim().isEmpty() 
                                    ? replyUser.getRealName() : replyUser.getUsername();
                            reply.setUserName(replyUserName);
                            reply.setUserAvatar(replyUser.getAvatar());
                            log.debug("设置回复用户名 - 回复ID: {}, 用户ID: {}, 用户名: {}", reply.getId(), reply.getUserId(), replyUserName);
                        } else {
                            log.warn("未找到回复用户 - 回复ID: {}, 用户ID: {}", reply.getId(), reply.getUserId());
                        }
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Feedback feedback) {
        // 校验参数
        if (feedback.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            throw new BusinessException("反馈内容不能为空");
        }
        
        // 如果是回复，则校验父级反馈是否存在
        if (feedback.getParentId() != null) {
            Feedback parent = getById(feedback.getParentId());
            if (parent == null) {
                throw new BusinessException("回复的反馈不存在");
            }
            
            // 设置回复的课程ID与父级反馈一致
            feedback.setCourseId(parent.getCourseId());
        }
        
        return super.save(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long id, Long userId, Integer role) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        // 查询反馈
        Feedback feedback = getById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        
        // 只有反馈的发布者或管理员可以删除反馈
        if (!feedback.getUserId().equals(userId) && role != 2) {
            throw new BusinessException("无权删除该反馈");
        }
        
        // 如果是父级反馈，则同时删除所有回复
        if (feedback.getParentId() == null) {
            LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Feedback::getParentId, id);
            remove(wrapper);
        }
        
        return removeById(id);
    }
} 