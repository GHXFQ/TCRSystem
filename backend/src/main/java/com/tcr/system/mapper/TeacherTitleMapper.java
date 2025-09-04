package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.TeacherTitle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师职称Mapper接口
 */
@Mapper
public interface TeacherTitleMapper extends BaseMapper<TeacherTitle> {
    
    /**
     * 根据教师ID获取职称列表
     * @param teacherId 教师ID
     * @return 职称列表
     */
    List<TeacherTitle> getTeacherTitlesByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据课程ID获取课程负责人ID
     * @param courseId 课程ID
     * @return 课程负责人ID
     */
    Long getCourseLeaderIdByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 根据专业ID获取专业负责人ID
     * @param majorId 专业ID
     * @return 专业负责人ID
     */
    Long getMajorLeaderIdByMajorId(@Param("majorId") Long majorId);
    
    /**
     * 根据学院ID获取副院长ID
     * @param collegeId 学院ID
     * @return 副院长ID
     */
    Long getViceDeanIdByCollegeId(@Param("collegeId") Long collegeId);
    
    /**
     * 获取学院所有副院长ID列表
     */
    List<Long> getViceDeanIdsByCollegeId(@Param("collegeId") Long collegeId);
} 