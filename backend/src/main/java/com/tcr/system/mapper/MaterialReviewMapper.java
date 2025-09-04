package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.MaterialReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 教学材料审核Mapper接口
 */
@Mapper
public interface MaterialReviewMapper extends BaseMapper<MaterialReview> {
    
    /**
     * 分页查询教师提交的材料审核列表
     * @param page 分页参数
     * @param teacherId 教师ID
     * @param academicYear 学年
     * @param semester 学期
     * @param status 状态
     * @return 分页结果
     */
    IPage<Map<String, Object>> getTeacherSubmitReviews(Page<Map<String, Object>> page, 
                                                      @Param("teacherId") Long teacherId,
                                                      @Param("academicYear") String academicYear,
                                                      @Param("semester") Integer semester,
                                                      @Param("status") Integer status);
    
    /**
     * 分页查询教师待审核的材料列表
     * @param page 分页参数
     * @param reviewerId 审核人ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 分页结果
     */
    IPage<Map<String, Object>> getTeacherPendingReviews(Page<Map<String, Object>> page, 
                                                       @Param("reviewerId") Long reviewerId,
                                                       @Param("academicYear") String academicYear,
                                                       @Param("semester") Integer semester);
    
    /**
     * 管理员获取审核列表
     */
    IPage<Map<String, Object>> getAdminReviewList(Page<Map<String, Object>> page,
                                                @Param("academicYear") String academicYear,
                                                @Param("semester") Integer semester,
                                                @Param("status") Integer status);
    
    /**
     * 获取审核详情
     */
    Map<String, Object> getReviewDetail(@Param("reviewId") Long reviewId);
    
    /**
     * 获取审核记录
     */
    List<Map<String, Object>> getReviewRecords(@Param("reviewId") Long reviewId);
} 