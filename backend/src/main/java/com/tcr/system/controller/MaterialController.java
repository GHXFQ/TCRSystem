package com.tcr.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Material;
import com.tcr.system.service.MaterialService;
import com.tcr.system.service.MaterialReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * 材料控制器
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "材料管理")
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialReviewService materialReviewService;
    private static final Logger log = LoggerFactory.getLogger(MaterialController.class);

    /**
     * 分页查询材料列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询材料列表")
    public Result<Page<Material>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("材料类型") @RequestParam(required = false) Integer type) {
        Page<Material> page = new Page<>(pageNum, pageSize);
        Page<Material> result = materialService.page(page, courseId, type);
        return Result.success(result);
    }
    
    /**
     * 查询教师上传的材料列表
     */
    @GetMapping("/teacher")
    @ApiOperation("查询教师上传的材料列表")
    public Result<Page<Map<String, Object>>> getTeacherMaterials(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("教师ID") @RequestParam Long teacherId,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("材料类型ID") @RequestParam(required = false) Long materialTypeId,
            @ApiParam("学年") @RequestParam(required = false) String academicYear,
            @ApiParam("学期") @RequestParam(required = false) Integer semester,
            @ApiParam("材料类型：0-课程材料，1-学生资料") @RequestParam(required = false) Integer type) {
        Page<Map<String, Object>> page = new Page<>(current, size);
        Page<Map<String, Object>> result = materialService.getTeacherMaterials(page, teacherId, courseId, materialTypeId, academicYear, semester, type);
        return Result.success(result);
    }

    /**
     * 上传材料
     */
    @PostMapping("/upload")
    @ApiOperation("上传材料")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> upload(
            @ApiParam("文件") @RequestParam("file") MultipartFile file,
            @ApiParam("材料名称") @RequestParam("name") String name,
            @ApiParam("材料描述") @RequestParam(value = "description", required = false) String description,
            @ApiParam("课程ID") @RequestParam("courseId") Long courseId,
            @ApiParam("材料类型：0-课程材料，1-学生资料") @RequestParam(value = "type", defaultValue = "0") Integer type,
            @ApiParam("学年") @RequestParam(value = "academicYear", required = false) String academicYear,
            @ApiParam("学期") @RequestParam(value = "semester", required = false) Integer semester,
            @ApiParam("材料类型ID") @RequestParam(value = "materialTypeId", required = false) Long materialTypeId,
            @ApiParam("任务ID") @RequestParam(value = "taskId", required = false) Long taskId,
            HttpServletRequest request) {
        
        // 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        
        try {
            // 添加日志，检查接收到的taskId
            log.info("上传材料接收到的参数 - name: {}, courseId: {}, type: {}, academicYear: {}, semester: {}, materialTypeId: {}, taskId: {}", 
                    name, courseId, type, academicYear, semester, materialTypeId, taskId);
            
            // 上传材料到临时表
            Long materialId;
            if (taskId != null) {
                // 如果提供了任务ID，使用任务材料上传方法
                log.info("使用任务材料上传方法，taskId: {}", taskId);
                materialId = materialService.uploadTaskMaterial(name, description, courseId, file, 
                                                           userId, type, academicYear, semester, 
                                                           materialTypeId, taskId);
            } else {
                // 否则使用普通材料上传方法
                log.info("使用普通材料上传方法，无taskId");
                materialId = materialService.uploadMaterial(name, description, courseId, file, 
                                                       userId, type, academicYear, semester, materialTypeId);
            }
            
            // 提交审核
            Long reviewId = materialReviewService.submitReview(materialId, academicYear, semester, 
                                                           materialTypeId, userId, courseId);
            
            return Result.success(reviewId);
        } catch (Exception e) {
            log.error("材料上传失败", e);
            return Result.error("材料上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载材料
     */
    @GetMapping("/download/{id}")
    @ApiOperation("下载材料")
    public void download(@ApiParam("材料ID") @PathVariable Long id, HttpServletResponse response) throws IOException {
        materialService.download(id, response);
    }

    /**
     * 删除材料
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除材料")
    public Result<Boolean> delete(@ApiParam("材料ID") @PathVariable Long id, @RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role) {
        boolean result = materialService.remove(id, userId, role);
        return Result.success(result, "删除成功");
    }

    /**
     * 分页查询课程资源协同列表
     */
    @GetMapping("/shared")
    @ApiOperation("分页查询课程资源协同列表")
    public Result<Page<Map<String, Object>>> getSharedMaterials(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
            @ApiParam("课程ID列表，用于权限控制") @RequestParam(required = false) Long[] courseIds,
            @ApiParam("课程ID列表，逗号分隔字符串") @RequestParam(required = false) String courseIdsStr,
            @ApiParam("材料类型ID") @RequestParam(required = false) Long materialTypeId,
            @ApiParam("学年") @RequestParam(required = false) String academicYear,
            @ApiParam("学期") @RequestParam(required = false) Integer semester,
            @ApiParam("材料类型：0-课程材料，1-学生资料") @RequestParam(required = false) Integer type,
            HttpServletRequest request) {
        
        // 从请求中获取用户ID和角色
        Long userId = (Long) request.getAttribute("userId");
        // 修复角色类型转换
        Object roleObj = request.getAttribute("role");
        Integer role = null;
        
        if (roleObj instanceof Integer) {
            role = (Integer) roleObj;
        } else if (roleObj instanceof String) {
            try {
                role = Integer.parseInt((String) roleObj);
            } catch (NumberFormatException e) {
                log.error("角色转换失败", e);
                return Result.error("角色信息无效");
            }
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        // 检查是否为教师角色
        if (role == null || role != 1) {
            return Result.error("无权限访问");
        }
        
        try {
            // 处理逗号分隔的课程ID字符串
            if (courseIds == null && courseIdsStr != null && !courseIdsStr.isEmpty()) {
                String[] courseIdStrings = courseIdsStr.split(",");
                courseIds = new Long[courseIdStrings.length];
                for (int i = 0; i < courseIdStrings.length; i++) {
                    try {
                        courseIds[i] = Long.parseLong(courseIdStrings[i].trim());
                    } catch (NumberFormatException e) {
                        log.warn("转换课程ID失败: {}", courseIdStrings[i]);
                    }
                }
            }
            
            Page<Map<String, Object>> page = new Page<>(current, pageSize);
            Page<Map<String, Object>> result = materialService.getSharedMaterials(page, userId, courseId, courseIds, materialTypeId, academicYear, semester, type);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取课程资源协同列表失败", e);
            return Result.error("获取课程资源协同列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取同一课程其他教师的教学大纲和授课计划
     */
    @GetMapping("/course-shared")
    @ApiOperation("获取同一课程其他教师的教学大纲和授课计划")
    public Result<Object> getCourseSharedMaterials(
            @ApiParam("课程ID") @RequestParam Long courseId,
            @ApiParam("材料类型ID") @RequestParam Long materialTypeId,
            @ApiParam("排除的用户ID") @RequestParam(required = false) Long excludeUserId,
            HttpServletRequest request) {
        
        // 从请求中获取用户ID和角色
        Long userId = (Long) request.getAttribute("userId");
        // 修复角色类型转换
        Object roleObj = request.getAttribute("role");
        Integer role = null;
        
        if (roleObj instanceof Integer) {
            role = (Integer) roleObj;
        } else if (roleObj instanceof String) {
            try {
                role = Integer.parseInt((String) roleObj);
            } catch (NumberFormatException e) {
                log.error("角色转换失败", e);
                return Result.error("角色信息无效");
            }
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        // 检查是否为教师角色
        if (role == null || role != 1) {
            return Result.error("无权限访问");
        }
        
        try {
            // 如果未指定排除用户，则默认排除当前用户
            if (excludeUserId == null) {
                excludeUserId = userId;
            }
            
            // 获取同一课程其他教师的教学大纲或授课计划
            return Result.success(materialService.getCourseSharedMaterials(courseId, materialTypeId, excludeUserId));
        } catch (Exception e) {
            log.error("获取同一课程其他教师的教学材料失败", e);
            return Result.error("获取同一课程其他教师的教学材料失败：" + e.getMessage());
        }
    }

    /**
     * 记录材料下载次数
     */
    @PostMapping("/download/{id}")
    @ApiOperation("记录材料下载次数")
    public Result<Object> updateDownloadCount(@ApiParam("材料ID") @PathVariable Long id) {
        try {
            materialService.updateDownloadCount(id);
            return Result.success(null, "记录下载次数成功");
        } catch (Exception e) {
            log.error("记录下载次数失败", e);
            return Result.error("记录下载次数失败：" + e.getMessage());
        }
    }
} 