package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学材料审核记录实体类
 */
@Data
@TableName("material_review_record")
public class MaterialReviewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审核ID
     */
    private Long reviewId;

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