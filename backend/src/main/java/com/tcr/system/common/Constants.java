package com.tcr.system.common;

/**
 * 常量类
 */
public class Constants {

    /**
     * 用户角色：学生
     */
    public static final Integer ROLE_STUDENT = 0;

    /**
     * 用户角色：教师
     */
    public static final Integer ROLE_TEACHER = 1;

    /**
     * 用户角色：管理员
     */
    public static final Integer ROLE_ADMIN = 2;

    /**
     * 用户状态：禁用
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 用户状态：启用
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 课程状态：未开始
     */
    public static final Integer COURSE_STATUS_NOT_STARTED = 0;

    /**
     * 课程状态：进行中
     */
    public static final Integer COURSE_STATUS_IN_PROGRESS = 1;

    /**
     * 课程状态：已结束
     */
    public static final Integer COURSE_STATUS_ENDED = 2;

    /**
     * 材料类型：课程材料
     */
    public static final Integer MATERIAL_TYPE_COURSE = 0;

    /**
     * 材料类型：学生资料
     */
    public static final Integer MATERIAL_TYPE_STUDENT = 1;

    /**
     * Redis中存储的Token前缀
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * Redis中存储的用户信息前缀
     */
    public static final String USER_PREFIX = "user:";

    /**
     * 文件上传路径
     */
    public static final String UPLOAD_PATH = "upload/";
} 