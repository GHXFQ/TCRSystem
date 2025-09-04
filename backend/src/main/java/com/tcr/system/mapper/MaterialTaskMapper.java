package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.MaterialTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 教学材料任务Mapper接口
 */
public interface MaterialTaskMapper extends BaseMapper<MaterialTask> {

    /**
     * 分页查询任务列表（包含材料类型名称）
     * @param page 分页对象
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @param status 状态
     * @param title 任务标题（模糊查询）
     * @return 分页结果
     */
    @Select({
        "<script>",
        "SELECT t.*, mt.name as material_type_name, ",
        "(SELECT GROUP_CONCAT(c.name SEPARATOR ', ') FROM course c WHERE FIND_IN_SET(c.id, t.course_ids) AND c.deleted = 0) as course_name ",
        "FROM material_task t ",
        "LEFT JOIN material_type mt ON t.material_type_id = mt.id ",
        "WHERE t.deleted = 0 ",
        "<if test='materialTypeId != null'>AND t.material_type_id = #{materialTypeId} </if>",
        "<if test='academicYear != null and academicYear != \"\"'>AND t.academic_year = #{academicYear} </if>",
        "<if test='semester != null'>AND t.semester = #{semester} </if>",
        "<if test='status != null'>AND t.status = #{status} </if>",
        "<if test='title != null and title != \"\"'>AND t.title LIKE CONCAT('%', #{title}, '%') </if>",
        "ORDER BY t.create_time DESC",
        "</script>"
    })
    IPage<MaterialTask> selectTaskPage(Page<MaterialTask> page,
                                       @Param("materialTypeId") Long materialTypeId,
                                       @Param("academicYear") String academicYear,
                                       @Param("semester") Integer semester,
                                       @Param("status") Integer status,
                                       @Param("title") String title);

    /**
     * 查询最近的任务
     * @param limit 限制数量
     * @return 任务列表
     */
    @Select("SELECT t.*, mt.name as material_type_name, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR ', ') FROM course c WHERE FIND_IN_SET(c.id, t.course_ids) AND c.deleted = 0) as course_name " +
            "FROM material_task t " +
            "LEFT JOIN material_type mt ON t.material_type_id = mt.id " +
            "WHERE t.deleted = 0 " +
            "ORDER BY t.create_time DESC " +
            "LIMIT #{limit}")
    List<MaterialTask> selectRecentTasks(@Param("limit") Integer limit);

    /**
     * 按材料类型统计已完成和未完成的任务数量
     * @return 统计结果列表，每项包含type_id, type_name, completed, uncompleted
     */
    @Select("SELECT " +
            "    mt.id as type_id, " +
            "    mt.name as type_name, " +
            "    COUNT(DISTINCT mtc.teacher_id) as completed, " +
            "    (SELECT COUNT(DISTINCT c.teacher_id) " +
            "     FROM material_task task " +
            "     JOIN course c ON FIND_IN_SET(c.id, task.course_ids) AND c.deleted = 0 " +
            "     WHERE task.material_type_id = mt.id AND task.deleted = 0 AND task.status = 1) - " +
            "    COUNT(DISTINCT mtc.teacher_id) as uncompleted " +
            "FROM material_type mt " +
            "LEFT JOIN material_task t ON mt.id = t.material_type_id AND t.deleted = 0 AND t.status = 1 " +
            "LEFT JOIN material_task_completion mtc ON t.id = mtc.task_id AND mtc.deleted = 0 " +
            "WHERE mt.deleted = 0 " +
            "GROUP BY mt.id, mt.name")
    List<Map<String, Object>> statisticsByType();

    /**
     * 按学院统计未完成任务的教师数量
     * @return 统计结果列表，每项包含college_id, college_name, count
     */
    @Select("SELECT c.id as college_id, c.name as college_name, COUNT(DISTINCT u.id) as count " +
            "FROM college c " +
            "JOIN major m ON c.id = m.college_id AND m.deleted = 0 " +
            "JOIN teacher_title tt ON m.id = tt.major_id AND tt.deleted = 0 " +
            "JOIN user u ON tt.teacher_id = u.id AND u.deleted = 0 AND u.role = 1 " +
            "LEFT JOIN course co ON u.id = co.teacher_id AND co.deleted = 0 " +
            "LEFT JOIN material_task t ON FIND_IN_SET(co.id, t.course_ids) AND t.deleted = 0 AND t.status = 1 " +
            "LEFT JOIN material_task_completion mtc ON t.id = mtc.task_id AND mtc.teacher_id = u.id AND mtc.course_id = co.id AND mtc.deleted = 0 " +
            "WHERE c.deleted = 0 AND mtc.id IS NULL AND t.id IS NOT NULL " +
            "GROUP BY c.id, c.name")
    List<Map<String, Object>> statisticsByCollege();

    /**
     * 查询教师负责的课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    @Select("SELECT c.* FROM course c WHERE c.teacher_id = #{teacherId} AND c.deleted = 0")
    List<com.tcr.system.entity.Course> selectCoursesByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查询与指定课程列表相关的任务
     * @param courseIds 课程ID列表
     * @return 任务列表
     */
    @Select({
        "<script>",
        "SELECT t.*, mt.name as material_type_name, ",
        "(SELECT GROUP_CONCAT(c.name SEPARATOR ', ') FROM course c WHERE FIND_IN_SET(c.id, t.course_ids) AND c.deleted = 0) as course_name ",
        "FROM material_task t ",
        "LEFT JOIN material_type mt ON t.material_type_id = mt.id ",
        "WHERE t.deleted = 0 AND t.status = 1 ",
        "<if test='courseIds != null and courseIds.size() > 0'>",
        "AND EXISTS (",
        "   SELECT 1 FROM course c ",
        "   WHERE c.deleted = 0 ",
        "   AND FIND_IN_SET(c.id, t.course_ids) ",
        "   AND c.id IN ",
        "   <foreach collection='courseIds' item='id' open='(' separator=',' close=')'>",
        "       #{id}",
        "   </foreach>",
        ")",
        "</if>",
        "ORDER BY t.create_time DESC",
        "</script>"
    })
    List<MaterialTask> selectTasksForTeacher(@Param("courseIds") List<Long> courseIds);
    
    /**
     * 查询与指定课程相关的教师ID列表
     * @param courseIds 课程ID列表
     * @return 教师ID列表
     */
    @Select({
        "<script>",
        "SELECT DISTINCT c.teacher_id FROM course c ",
        "WHERE c.deleted = 0 ",
        "AND c.id IN ",
        "<foreach collection='courseIds' item='id' open='(' separator=',' close=')'>",
        "   #{id}",
        "</foreach>",
        "</script>"
    })
    List<Long> selectTeacherIdsWithCourses(@Param("courseIds") List<Long> courseIds);
    
    /**
     * 分页查询未完成任务的教师列表
     * @param page 分页对象
     * @param taskIds 任务ID列表
     * @param courseIds 课程ID列表
     * @param materialTypeId 材料类型ID（可选）
     * @param academicYear 学年（可选）
     * @param semester 学期（可选）
     * @return 分页结果
     */
    @Select({
        "<script>",
        "SELECT u.id as teacherId, u.real_name as teacherName, u.username, u.email, u.phone, ",
        "co.id as courseId, co.name as courseName, ",
        "( ",
        "  SELECT c.name ",
        "  FROM user u2 ",
        "  LEFT JOIN teacher_title tt ON u2.id = tt.teacher_id AND tt.deleted = 0 ",
        "  LEFT JOIN major m ON tt.major_id = m.id AND m.deleted = 0 ",
        "  LEFT JOIN college c ON m.college_id = c.id AND c.deleted = 0 ",
        "  WHERE u2.id = u.id ",
        "  LIMIT 1 ",
        ") as collegeName, ",
        "task.id as taskId, ",
        "task.title as taskTitle, ",
        "mt.id as materialTypeId, ",
        "mt.name as materialTypeName, ",
        "task.academic_year as academicYear, ",
        "task.semester as semester, ",
        "task.deadline as deadline ",
        "FROM user u ",
        "JOIN course co ON u.id = co.teacher_id AND co.deleted = 0 ",
        "JOIN material_task task ON FIND_IN_SET(co.id, task.course_ids) AND task.deleted = 0 AND task.status = 1 ",
        "LEFT JOIN material_type mt ON task.material_type_id = mt.id AND mt.deleted = 0 ",
        "LEFT JOIN material_task_completion mtc ON task.id = mtc.task_id AND mtc.teacher_id = u.id AND mtc.course_id = co.id AND mtc.deleted = 0 ",
        "WHERE u.deleted = 0 AND u.role = 1 ", // 只查询教师角色
        "AND co.id IN ",
        "<foreach collection='courseIds' item='id' open='(' separator=',' close=')'>",
        "   #{id}",
        "</foreach>",
        "AND task.id IN ",
        "<foreach collection='taskIds' item='id' open='(' separator=',' close=')'>",
        "   #{id}",
        "</foreach>",
        "AND mtc.id IS NULL ", // 未完成的记录没有对应的完成记录
        "<if test='materialTypeId != null'>AND task.material_type_id = #{materialTypeId} </if>",
        "<if test='academicYear != null and academicYear != \"\"'>AND task.academic_year = #{academicYear} </if>",
        "<if test='semester != null'>AND task.semester = #{semester} </if>",
        "ORDER BY task.deadline ASC, u.real_name ASC",
        "</script>"
    })
    IPage<Map<String, Object>> selectUncompletedTeachersPage(
        Page<Map<String, Object>> page,
        @Param("taskIds") List<Long> taskIds,
        @Param("courseIds") List<Long> courseIds,
        @Param("materialTypeId") Long materialTypeId,
        @Param("academicYear") String academicYear,
        @Param("semester") Integer semester
    );
} 