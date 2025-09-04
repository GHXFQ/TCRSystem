package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.Assignment;
import com.tcr.system.vo.AssignmentVO;

import java.util.List;
import java.util.Map;

/**
 * 作业Service接口
 */
public interface AssignmentService extends IService<Assignment> {

    /**
     * 获取教师发布的作业列表
     * @param page 分页参数
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param status 状态
     * @return 分页结果
     */
    Page<AssignmentVO> getTeacherAssignments(Page<Assignment> page, Long teacherId, Long courseId, Integer status);
    
    /**
     * 获取学生的作业列表
     * @param page 分页参数
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param status 状态
     * @return 分页结果
     */
    Page<AssignmentVO> getStudentAssignments(Page<Assignment> page, Long studentId, Long courseId, Integer status);
    
    /**
     * 发布作业
     * @param assignment 作业信息
     * @return 作业ID
     */
    Long publishAssignment(Assignment assignment);
    
    /**
     * 更新作业状态
     * @param id 作业ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 获取作业详情
     * @param id 作业ID
     * @return 作业详情
     */
    AssignmentVO getAssignmentDetail(Long id);
    
    /**
     * 获取作业统计数据
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 统计数据
     */
    Map<String, Object> getAssignmentStatistics(Long teacherId, Long courseId);
    
    /**
     * 获取作业提交率信息
     * @param teacherId 教师ID
     * @return 提交率信息
     */
    List<Map<String, Object>> getSubmissionRateInfo(Long teacherId);
    
    /**
     * 自动更新作业状态
     * 检查截止日期，自动将已过期的作业状态更新为已结束
     */
    void autoUpdateAssignmentStatus();
} 