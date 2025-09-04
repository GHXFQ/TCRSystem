package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.Feedback;

/**
 * 反馈服务接口
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 分页查询反馈列表
     *
     * @param page 分页参数
     * @param courseId 课程ID
     * @return 分页结果
     */
    Page<Feedback> page(Page<Feedback> page, Long courseId);

    /**
     * 新增反馈
     *
     * @param feedback 反馈信息
     * @return 是否成功
     */
    boolean save(Feedback feedback);

    /**
     * 删除反馈
     *
     * @param id 反馈ID
     * @param userId 用户ID
     * @param role 用户角色
     * @return 是否成功
     */
    boolean remove(Long id, Long userId, Integer role);
} 