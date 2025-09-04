package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcr.system.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班级Mapper接口
 */
@Mapper
public interface ClassMapper extends BaseMapper<Class> {

    /**
     * 获取班级详情，包含专业和学院信息
     *
     * @param id 班级ID
     * @return 班级详情
     */
    @Select("SELECT c.*, m.name as major_name, co.name as college_name " +
            "FROM class c " +
            "LEFT JOIN major m ON c.major_id = m.id " +
            "LEFT JOIN college co ON m.college_id = co.id " +
            "WHERE c.id = #{id} AND c.deleted = 0")
    Class getClassDetail(@Param("id") Long id);

    /**
     * 获取班级列表，包含专业和学院信息
     *
     * @return 班级列表
     */
    @Select("SELECT c.*, m.name as major_name, co.name as college_name " +
            "FROM class c " +
            "LEFT JOIN major m ON c.major_id = m.id " +
            "LEFT JOIN college co ON m.college_id = co.id " +
            "WHERE c.deleted = 0")
    List<Class> getClassList();
    
    /**
     * 获取班级学生数量
     *
     * @param classId 班级ID
     * @return 学生数量
     */
    @Select("SELECT COUNT(1) FROM user WHERE class_id = #{classId} AND role = 0 AND deleted = 0")
    Integer getStudentCount(@Param("classId") Long classId);
} 