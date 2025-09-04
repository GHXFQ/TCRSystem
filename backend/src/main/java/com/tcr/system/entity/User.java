package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名/学号/工号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色：0-学生，1-教师，2-管理员
     */
    private Integer role;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称（非数据库字段）
     */
    @TableField(exist = false)
    private String className;
    
    /**
     * 专业名称（非数据库字段）
     */
    @TableField(exist = false)
    private String majorName;
    
    /**
     * 学院名称（非数据库字段）
     */
    @TableField(exist = false)
    private String collegeName;

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