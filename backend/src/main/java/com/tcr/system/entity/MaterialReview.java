package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学材料审核实体类
 */
@Data
@TableName("material_review")
public class MaterialReview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 状态：0-待审核，1-课程负责人审核中，2-专业负责人审核中，3-副院长审核中，4-教务人员审核中，5-审核通过，6-审核不通过
     */
    private Integer status;

    /**
     * 当前审核人ID
     */
    private Long currentReviewerId;

    /**
     * 提交用户ID
     */
    private Long submitUserId;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 所属专业ID
     */
    private Long majorId;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 