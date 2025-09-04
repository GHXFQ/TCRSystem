package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.MaterialTaskCompletion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 教学材料任务完成记录Mapper接口
 */
public interface MaterialTaskCompletionMapper extends BaseMapper<MaterialTaskCompletion> {

    /**
     * 查询任务完成情况
     * @param taskId 任务ID
     * @param page 分页对象
     * @return 已完成的记录，带有教师、课程和材料信息
     */
    @Select("SELECT mtc.*, u.real_name as teacher_name, c.name as course_name, m.name as material_name " +
            "FROM material_task_completion mtc " +
            "JOIN user u ON mtc.teacher_id = u.id " +
            "JOIN course c ON mtc.course_id = c.id " +
            "JOIN material m ON mtc.material_id = m.id " +
            "WHERE mtc.task_id = #{taskId} AND mtc.deleted = 0 " +
            "ORDER BY mtc.completion_time DESC")
    IPage<MaterialTaskCompletion> selectCompletionsByTaskId(Page<MaterialTaskCompletion> page, @Param("taskId") Long taskId);

    /**
     * 查询任务完成统计信息
     * @param taskId 任务ID
     * @return 包含总数、已完成数和未完成数的Map
     */
    @Select("SELECT " +
            "(SELECT COUNT(DISTINCT c.teacher_id) FROM course c WHERE FIND_IN_SET(c.id, (SELECT course_ids FROM material_task WHERE id = #{taskId})) AND c.deleted = 0) as total_count, " +
            "COUNT(DISTINCT mtc.teacher_id) as completed_count " +
            "FROM material_task_completion mtc " +
            "WHERE mtc.task_id = #{taskId} AND mtc.deleted = 0")
    Map<String, Object> selectCompletionStatsByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询未完成任务的教师列表
     * @param taskId 任务ID
     * @return 未完成任务的教师信息列表
     */
    @Select("SELECT u.id as teacher_id, u.real_name as teacher_name, u.email, u.phone, c.id as course_id, c.name as course_name " +
            "FROM course c " +
            "JOIN user u ON c.teacher_id = u.id AND u.deleted = 0 AND u.role = 1 " +
            "JOIN material_task mt ON FIND_IN_SET(c.id, mt.course_ids) AND mt.id = #{taskId} AND mt.deleted = 0 " +
            "LEFT JOIN material_task_completion mtc ON mt.id = mtc.task_id AND mtc.teacher_id = u.id AND mtc.course_id = c.id AND mtc.deleted = 0 " +
            "WHERE c.deleted = 0 AND mtc.id IS NULL")
    List<Map<String, Object>> selectUncompletedTeachersByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询统计概览数据
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 统计概览数据
     */
    @Select({
        "<script>",
        "SELECT ",
        "  COUNT(DISTINCT m.id) as total_materials, ",
        "  COUNT(DISTINCT mtc.teacher_id) as completed_teachers, ",
        "  (SELECT COUNT(DISTINCT u.id) FROM user u JOIN course c ON u.id = c.teacher_id WHERE u.role = 1 AND u.deleted = 0 AND c.deleted = 0) - COUNT(DISTINCT mtc.teacher_id) as uncompleted_teachers ",
        "FROM material m ",
        "JOIN material_task_completion mtc ON m.id = mtc.material_id ",
        "JOIN material_task mt ON mtc.task_id = mt.id ",
        "WHERE m.deleted = 0 AND mtc.deleted = 0 AND mt.deleted = 0 ",
        "<if test='materialTypeId != null'>AND mt.material_type_id = #{materialTypeId} </if>",
        "<if test='academicYear != null and academicYear != \"\"'>AND mt.academic_year = #{academicYear} </if>",
        "<if test='semester != null'>AND mt.semester = #{semester} </if>",
        "</script>"
    })
    Map<String, Object> selectStatisticsOverview(@Param("materialTypeId") Long materialTypeId,
                                               @Param("academicYear") String academicYear,
                                               @Param("semester") Integer semester);

    /**
     * 查询未完成任务的教师列表（带分页）
     * @param page 分页对象
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 未完成任务的教师信息列表
     */
    @Select({
        "<script>",
        "SELECT u.id as teacher_id, u.real_name as teacher_name, u.email, u.phone, ",
        "  c.id as course_id, c.name as course_name, ",
        "  col.id as college_id, col.name as college_name, ",
        "  mt.id as task_id, mt.title as task_title, mt.deadline, ",
        "  mt.academic_year, mt.semester, ",
        "  mty.id as material_type_id, mty.name as material_type_name ",
        "FROM user u ",
        "JOIN course c ON u.id = c.teacher_id AND c.deleted = 0 ",
        "LEFT JOIN teacher_title tt ON u.id = tt.teacher_id AND tt.deleted = 0 ",
        "LEFT JOIN major m ON tt.major_id = m.id AND m.deleted = 0 ",
        "LEFT JOIN college col ON m.college_id = col.id AND col.deleted = 0 ",
        "JOIN material_task mt ON FIND_IN_SET(c.id, mt.course_ids) AND mt.deleted = 0 AND mt.status = 1 ",
        "JOIN material_type mty ON mt.material_type_id = mty.id AND mty.deleted = 0 ",
        "LEFT JOIN material_task_completion mtc ON mt.id = mtc.task_id AND mtc.teacher_id = u.id AND mtc.course_id = c.id AND mtc.deleted = 0 ",
        "WHERE u.deleted = 0 AND u.role = 1 AND mtc.id IS NULL ",
        "<if test='materialTypeId != null'>AND mt.material_type_id = #{materialTypeId} </if>",
        "<if test='academicYear != null and academicYear != \"\"'>AND mt.academic_year = #{academicYear} </if>",
        "<if test='semester != null'>AND mt.semester = #{semester} </if>",
        "ORDER BY mt.deadline ASC, u.real_name ASC",
        "</script>"
    })
    IPage<Map<String, Object>> selectUncompletedTeachersPage(Page<Map<String, Object>> page,
                                                            @Param("materialTypeId") Long materialTypeId,
                                                            @Param("academicYear") String academicYear,
                                                            @Param("semester") Integer semester);
} 