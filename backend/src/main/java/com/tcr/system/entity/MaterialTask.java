package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tcr.system.config.StringListConverter;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学材料任务实体类
 */
@Data
@TableName("material_task")
public class MaterialTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 材料类型ID
     */
    private Long materialTypeId;

    /**
     * 课程ID列表，以逗号分隔
     * 支持前端传递数组格式，将自动转换为逗号分隔的字符串
     */
    @JsonDeserialize(using = StringListConverter.Deserializer.class)
    private String courseIds;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 学期：1-第一学期，2-第二学期
     */
    private Integer semester;

    /**
     * 截止日期
     */
    private LocalDateTime deadline;

    /**
     * 状态：0-未发布，1-进行中，2-已结束
     */
    private Integer status;

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

    /**
     * 非数据库字段 - 材料类型名称
     */
    @TableField(exist = false)
    private String materialTypeName;

    /**
     * 非数据库字段 - 课程名称（多个课程时使用逗号分隔）
     */
    @TableField(exist = false)
    private String courseName;

    /**
     * 非数据库字段 - 任务是否已完成
     */
    @TableField(exist = false)
    private Boolean completed;

    /**
     * 非数据库字段 - 已完成任务关联的材料ID
     */
    @TableField(exist = false)
    private Long materialId;
} 