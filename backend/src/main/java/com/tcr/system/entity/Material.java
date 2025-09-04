package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 教学材料实体类
 */
@Data
@TableName("material")
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 材料ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 材料名称
     */
    private String name;

    /**
     * 材料描述
     */
    private String description;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 材料类型：0-课程材料，1-学生资料
     */
    private Integer type;
    
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
     * 关联的任务ID
     */
    private Long taskId;

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
     * 非数据库字段 - 额外标签信息
     */
    @TableField(exist = false)
    private Map<String, Object> tagInfo;
    
    /**
     * 非数据库字段 - 上传者名称
     */
    @TableField(exist = false)
    private String uploadUserName;
} 