package com.tcr.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("temp_material")
public class TempMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Long uploadUserId;
    private Integer type;
    private String academicYear;
    private Integer semester;
    private Long materialTypeId;
    private Long taskId;
    private Boolean deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 