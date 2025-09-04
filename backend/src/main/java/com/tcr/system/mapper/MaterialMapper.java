package com.tcr.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 材料Mapper接口
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
    
    /**
     * 查询教师上传的材料列表
     * 
     * @param page 分页参数
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @param type 材料类型：0-课程材料，1-学生资料
     * @return 分页结果
     */
    Page<Map<String, Object>> getTeacherMaterials(Page<Map<String, Object>> page, 
                                               @Param("teacherId") Long teacherId,
                                               @Param("courseId") Long courseId,
                                               @Param("materialTypeId") Long materialTypeId,
                                               @Param("academicYear") String academicYear,
                                               @Param("semester") Integer semester,
                                               @Param("type") Integer type);

    /**
     * 查询课程资源协同列表
     * 
     * @param page 分页参数
     * @param userId 当前用户ID
     * @param courseId 课程ID
     * @param courseIds 用户可访问的课程ID列表
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @param type 材料类型：0-课程材料，1-学生资料
     * @return 分页结果
     */
    Page<Map<String, Object>> getSharedMaterials(Page<Map<String, Object>> page, 
                                             @Param("userId") Long userId,
                                             @Param("courseId") Long courseId,
                                             @Param("courseIds") Long[] courseIds,
                                             @Param("materialTypeId") Long materialTypeId,
                                             @Param("academicYear") String academicYear,
                                             @Param("semester") Integer semester,
                                             @Param("type") Integer type);
                                             
    /**
     * 获取同一课程其他教师的教学大纲和授课计划
     * 
     * @param courseId 课程ID
     * @param materialTypeId 材料类型ID
     * @param excludeUserId 排除的用户ID
     * @return 材料列表
     */
    java.util.List<Map<String, Object>> getCourseSharedMaterials(@Param("courseId") Long courseId, 
                                                            @Param("materialTypeId") Long materialTypeId, 
                                                            @Param("excludeUserId") Long excludeUserId);
} 