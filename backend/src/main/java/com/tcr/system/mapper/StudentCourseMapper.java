package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.StudentCourse;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学生课程Mapper接口
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {
    
    /**
     * 查询记录，包括已逻辑删除的记录
     * @param wrapper 查询条件
     * @return 查询结果
     */
    List<Map<String, Object>> selectMapsForLogicDelete(@Param("ew") Wrapper<StudentCourse> wrapper);
    
    /**
     * 物理删除记录
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM student_course WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int deletePhysically(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
} 