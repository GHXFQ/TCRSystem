package com.tcr.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.MaterialReview;

import java.util.List;
import java.util.Map;

/**
 * 教学材料审核Service接口
 */
public interface MaterialReviewService extends IService<MaterialReview> {
    
    /**
     * 提交材料审核
     * @param materialId 材料ID
     * @param academicYear 学年
     * @param semester 学期
     * @param materialTypeId 材料类型ID
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 审核ID
     */
    Long submitReview(Long materialId, String academicYear, Integer semester, Long materialTypeId, Long teacherId, Long courseId);
    
    /**
     * 审核材料
     * @param reviewId 审核ID
     * @param reviewerId 审核人ID
     * @param reviewerTitle 审核人职称
     * @param result 审核结果：0-不通过，1-通过
     * @param comment 审核意见
     * @return 是否成功
     */
    boolean reviewMaterial(Long reviewId, Long reviewerId, Integer reviewerTitle, Integer result, String comment);
    
    /**
     * 分页查询教师提交的材料审核列表
     * @param page 分页参数
     * @param teacherId 教师ID
     * @param academicYear 学年
     * @param semester 学期
     * @param status 状态
     * @return 分页结果
     */
    IPage<Map<String, Object>> getTeacherSubmitReviews(Page<Map<String, Object>> page, Long teacherId, String academicYear, Integer semester, Integer status);
    
    /**
     * 分页查询教师待审核的材料列表
     * @param page 分页参数
     * @param reviewerId 审核人ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 分页结果
     */
    IPage<Map<String, Object>> getTeacherPendingReviews(Page<Map<String, Object>> page, Long reviewerId, String academicYear, Integer semester);
    
    /**
     * 获取材料审核详情
     * @param reviewId 审核ID
     * @return 审核详情
     */
    Map<String, Object> getReviewDetail(Long reviewId);
    
    /**
     * 获取审核记录列表
     * @param reviewId 审核ID
     * @return 审核记录列表
     */
    List<Map<String, Object>> getReviewRecords(Long reviewId);
    
    /**
     * 管理员获取审核列表
     */
    IPage<Map<String, Object>> getAdminReviewList(Page<Map<String, Object>> page, String academicYear, Integer semester, Integer status);
} 