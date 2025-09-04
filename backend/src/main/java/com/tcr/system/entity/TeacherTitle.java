package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教师职称实体类
 */
@Data
@TableName("teacher_title")
public class TeacherTitle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 职称类型：0-普通教师，1-课程负责人，2-专业负责人，3-副院长
     */
    private Integer titleType;

    /**
     * 所负责课程ID，仅当title_type=1时有效
     */
    private Long courseId;

    /**
     * 所负责专业ID，仅当title_type=2时有效
     */
    private Long majorId;

    /**
     * 所属学院ID，仅当title_type=3时有效
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