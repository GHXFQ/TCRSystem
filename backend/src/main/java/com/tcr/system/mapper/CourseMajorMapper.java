package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.CourseMajor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程专业关联Mapper接口
 */
@Mapper
public interface CourseMajorMapper extends BaseMapper<CourseMajor> {
    
    /**
     * 根据课程ID获取专业ID列表
     * @param courseId 课程ID
     * @return 专业ID列表
     */
    List<Long> getMajorIdsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 根据专业ID获取课程ID列表
     * @param majorId 专业ID
     * @return 课程ID列表
     */
    List<Long> getCourseIdsByMajorId(@Param("majorId") Long majorId);
} 