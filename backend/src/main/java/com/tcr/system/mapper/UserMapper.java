package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 分页查询教师列表
     * @param page 分页参数
     * @param realName 教师姓名
     * @param username 工号
     * @param titleType 职称类型
     * @return 教师列表
     */
    @Select({
        "<script>",
        "SELECT u.*, GROUP_CONCAT(DISTINCT tt.title_type) AS title_types",
        "FROM user u",
        "LEFT JOIN teacher_title tt ON u.id = tt.teacher_id AND tt.deleted = 0",
        "WHERE u.deleted = 0 AND u.role = 1",
        "<if test='realName != null and realName != \"\"'>",
        "  AND u.real_name LIKE CONCAT('%', #{realName}, '%')",
        "</if>",
        "<if test='username != null and username != \"\"'>",
        "  AND u.username LIKE CONCAT('%', #{username}, '%')",
        "</if>",
        "<if test='titleType != null'>",
        "  AND EXISTS (SELECT 1 FROM teacher_title t WHERE t.teacher_id = u.id AND t.title_type = #{titleType} AND t.deleted = 0)",
        "</if>",
        "GROUP BY u.id",
        "</script>"
    })
    Page<User> getTeacherList(Page<User> page, @Param("realName") String realName, 
                             @Param("username") String username, @Param("titleType") Integer titleType);

    /**
     * 分页查询学生列表
     * @param page 分页参数
     * @param realName 学生姓名
     * @param username 学号
     * @param classId 班级ID
     * @return 学生列表
     */
    @Select({
        "<script>",
        "SELECT u.*, c.name AS className",
        "FROM user u",
        "LEFT JOIN class c ON u.class_id = c.id AND c.deleted = 0",
        "WHERE u.deleted = 0 AND u.role = 0",
        "<if test='realName != null and realName != \"\"'>",
        "  AND u.real_name LIKE CONCAT('%', #{realName}, '%')",
        "</if>",
        "<if test='username != null and username != \"\"'>",
        "  AND u.username LIKE CONCAT('%', #{username}, '%')",
        "</if>",
        "<if test='classId != null'>",
        "  AND u.class_id = #{classId}",
        "</if>",
        "</script>"
    })
    Page<User> getStudentList(Page<User> page, @Param("realName") String realName, 
                             @Param("username") String username, @Param("classId") Long classId);
    
    /**
     * 根据ID获取学生详细信息（包括班级、专业和学院信息）
     * @param userId 用户ID
     * @return 学生信息
     */
    @Select({
        "SELECT u.*, c.name AS className, m.name AS majorName, col.name AS collegeName ",
        "FROM user u ",
        "LEFT JOIN class c ON u.class_id = c.id AND c.deleted = 0 ",
        "LEFT JOIN major m ON c.major_id = m.id AND m.deleted = 0 ",
        "LEFT JOIN college col ON m.college_id = col.id AND col.deleted = 0 ",
        "WHERE u.id = #{userId} AND u.deleted = 0"
    })
    User getStudentDetailById(@Param("userId") Long userId);
    
    /**
     * 根据ID获取教师详细信息（包括学院信息）
     * @param userId 用户ID
     * @return 教师信息
     */
    @Select({
        "SELECT u.*, col.name AS collegeName ",
        "FROM user u ",
        "LEFT JOIN teacher_title tt ON u.id = tt.teacher_id AND tt.deleted = 0 ",
        "LEFT JOIN college col ON tt.college_id = col.id AND col.deleted = 0 ",
        "WHERE u.id = #{userId} AND u.deleted = 0 ",
        "LIMIT 1"
    })
    User getTeacherDetailById(@Param("userId") Long userId);
} 