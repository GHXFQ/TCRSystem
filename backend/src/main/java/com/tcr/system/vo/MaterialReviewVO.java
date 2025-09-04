package com.tcr.system.vo;

import lombok.Data;

/**
 * 教学材料审核VO类
 */
@Data
public class MaterialReviewVO {
    
    /**
     * 审核ID
     */
    private Long reviewId;
    
    /**
     * 材料ID
     */
    private Long materialId;
    
    /**
     * 学年
     */
    private String academicYear;
    
    /**
     * 学期：1-第一学期，2-第二学期
     */
    private Integer semester;
    
    /**
     * 材料类型ID
     */
    private Long materialTypeId;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核人职称：0-普通教师，1-课程负责人，2-专业负责人，3-副院长，4-教务人员
     */
    private Integer reviewerTitle;
    
    /**
     * 审核结果：0-不通过，1-通过
     */
    private Integer result;
    
    /**
     * 审核意见
     */
    private String comment;
} 