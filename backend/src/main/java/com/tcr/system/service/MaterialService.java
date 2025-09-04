package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.entity.Material;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 材料服务接口
 */
public interface MaterialService extends IService<Material> {

    /**
     * 分页查询材料列表
     *
     * @param page 分页参数
     * @param courseId 课程ID
     * @param type 材料类型
     * @return 分页结果
     */
    Page<Material> page(Page<Material> page, Long courseId, Integer type);

    /**
     * 根据ID查询材料
     *
     * @param id 材料ID
     * @return 材料信息
     */
    Material getById(Long id);
    
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
    Page<Map<String, Object>> getTeacherMaterials(Page<Map<String, Object>> page, Long teacherId, 
                                                Long courseId, Long materialTypeId, 
                                                String academicYear, Integer semester, Integer type);

    /**
     * 上传材料
     */
    Long uploadMaterial(String name, String description, Long courseId, MultipartFile file,
                    Long uploadUserId, Integer type, String academicYear, Integer semester,
                    Long materialTypeId);
                    
    /**
     * 上传任务相关的材料
     */
    Long uploadTaskMaterial(String name, String description, Long courseId, MultipartFile file,
                    Long uploadUserId, Integer type, String academicYear, Integer semester,
                    Long materialTypeId, Long taskId);

    /**
     * 下载材料
     */
    void download(Long id, HttpServletResponse response) throws IOException;

    /**
     * 删除材料
     *
     * @param id 材料ID
     * @param userId 用户ID
     * @param role 用户角色
     * @return 是否成功
     */
    boolean remove(Long id, Long userId, Integer role);
    
    /**
     * 查询课程资源协同列表
     *
     * @param page 分页参数
     * @param userId 当前用户ID
     * @param courseId 课程ID
     * @param courseIds 用户可访问的课程ID列表，用于权限控制
     * @param materialTypeId 材料类型ID
     * @param academicYear 学年
     * @param semester 学期
     * @param type 材料类型：0-课程材料，1-学生资料
     * @return 分页结果
     */
    Page<Map<String, Object>> getSharedMaterials(Page<Map<String, Object>> page, Long userId, 
                                              Long courseId, Long[] courseIds, Long materialTypeId, 
                                              String academicYear, Integer semester, Integer type);
    
    /**
     * 获取同一课程其他教师的教学大纲和授课计划
     *
     * @param courseId 课程ID
     * @param materialTypeId 材料类型ID
     * @param excludeUserId 排除的用户ID（当前用户）
     * @return 材料列表
     */
    Object getCourseSharedMaterials(Long courseId, Long materialTypeId, Long excludeUserId);
    
    /**
     * 更新材料下载次数
     *
     * @param id 材料ID
     */
    void updateDownloadCount(Long id);
} 