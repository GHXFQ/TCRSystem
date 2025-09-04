package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 作业Mapper接口
 */
@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {

    /**
     * 获取作业平均分
     * @param assignmentId 作业ID
     * @return 平均分
     */
    @Select("SELECT AVG(score) FROM assignment_submission WHERE assignment_id = #{assignmentId} AND status = 1 AND score IS NOT NULL AND deleted = 0")
    Double getAssignmentAvgScore(@Param("assignmentId") Long assignmentId);
    
    /**
     * 获取教师各课程的作业数量
     * @param teacherId 教师ID
     * @return 各课程的作业数量
     */
    @Select("SELECT c.id as courseId, c.name as courseName, COUNT(a.id) as count " +
            "FROM assignment a " +
            "JOIN course c ON a.course_id = c.id " +
            "WHERE a.teacher_id = #{teacherId} AND a.deleted = 0 " +
            "GROUP BY a.course_id")
    List<Map<String, Object>> getCourseAssignmentCount(@Param("teacherId") Long teacherId);
} 