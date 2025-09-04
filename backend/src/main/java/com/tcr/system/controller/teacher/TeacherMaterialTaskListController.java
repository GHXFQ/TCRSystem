package com.tcr.system.controller.teacher;

import com.tcr.system.common.Result;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.entity.MaterialTaskCompletion;
import com.tcr.system.mapper.MaterialTaskCompletionMapper;
import com.tcr.system.mapper.MaterialTaskMapper;
import com.tcr.system.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("/api/material-task/teacher")
@Api(tags = "教师任务管理")
public class TeacherMaterialTaskListController {
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherMaterialTaskListController.class);

    @Autowired
    private MaterialTaskMapper materialTaskMapper;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private MaterialTaskCompletionMapper materialTaskCompletionMapper;
    
    @ApiOperation("获取教师的任务列表")
    @GetMapping("/tasks")
    public Result<List<MaterialTask>> getTeacherTasks(HttpServletRequest request, @RequestParam(required = false) Long teacherId) {
        logger.info("接收到获取教师任务列表请求");
        
        try {
            // 从请求中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (teacherId == null) {
                teacherId = userId; // 如果未传入teacherId参数，则使用当前登录用户ID
            }
            
            logger.info("处理教师任务列表请求，教师ID: {}", teacherId);
            
            if (teacherId == null) {
                logger.error("未获取到有效的教师ID");
                return Result.error("未登录");
            }
            
            // 直接发起两个单独的查询，减少级联失败的风险
            
            // 1. 添加调试信息 - 先查询整个课程表，检查数据是否存在
            List<Course> allCourses = courseService.list();
            logger.info("系统中所有课程数量: {}", allCourses != null ? allCourses.size() : 0);
            
            // 2. 查询教师负责的课程
            List<Course> courses = materialTaskMapper.selectCoursesByTeacherId(teacherId);
            logger.info("教师 {} 负责的课程数量: {}", teacherId, courses != null ? courses.size() : 0);
            
            List<Long> courseIds = new ArrayList<>();
            if (courses != null && !courses.isEmpty()) {
                // 提取课程ID列表
                courseIds = courses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
                
                logger.info("教师 {} 负责的课程ID列表: {}", teacherId, courseIds);
            } else {
                logger.info("教师 {} 没有负责的课程，返回空列表", teacherId);
                return Result.success(new ArrayList<>());
            }
            
            // 3. 先查询所有任务，检查数据是否存在
            List<MaterialTask> allTasks = materialTaskMapper.selectList(null);
            logger.info("系统中所有任务数量: {}", allTasks != null ? allTasks.size() : 0);
            
            // 4. 查询与课程相关的任务
            List<MaterialTask> tasks = materialTaskMapper.selectTasksForTeacher(courseIds);
            logger.info("教师 {} 相关的任务数量: {}", teacherId, tasks != null ? tasks.size() : 0);
            
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            
            // 5. 查询该教师已完成的任务记录
            if (!tasks.isEmpty()) {
                logger.info("开始查询教师 {} 的任务完成情况", teacherId);
                // 获取所有任务ID
                List<Long> taskIds = tasks.stream()
                        .map(MaterialTask::getId)
                        .collect(Collectors.toList());
                
                // 查询该教师的任务完成记录
                QueryWrapper<MaterialTaskCompletion> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teacher_id", teacherId)
                         .in("task_id", taskIds)
                         .in("course_id", courseIds)
                         .eq("deleted", 0);
                
                List<MaterialTaskCompletion> completions = materialTaskCompletionMapper.selectList(queryWrapper);
                logger.info("教师 {} 已完成的任务记录数量: {}", teacherId, completions.size());
                
                // 创建已完成任务的Map，key为"taskId-courseId"
                Map<String, MaterialTaskCompletion> completionMap = new HashMap<>();
                for (MaterialTaskCompletion completion : completions) {
                    String key = completion.getTaskId() + "-" + completion.getCourseId();
                    completionMap.put(key, completion);
                }
                
                // 为每个任务设置completed标志
                for (MaterialTask task : tasks) {
                    boolean isCompleted = false;
                    
                    // 检查该任务的每个关联课程是否有完成记录
                    String courseIdsStr = task.getCourseIds();
                    if (courseIdsStr != null && !courseIdsStr.isEmpty()) {
                        String[] ids = courseIdsStr.split(",");
                        for (String courseId : ids) {
                            if (courseIds.contains(Long.parseLong(courseId.trim()))) {
                                String key = task.getId() + "-" + courseId.trim();
                                if (completionMap.containsKey(key)) {
                                    isCompleted = true;
                                    // 设置materialId以便前端可以查看已上传的材料
                                    task.setMaterialId(completionMap.get(key).getMaterialId());
                                    break;
                                }
                            }
                        }
                    }
                    
                    // 设置任务的完成状态
                    task.setCompleted(isCompleted);
                    logger.debug("任务 {} 完成状态: {}", task.getId(), isCompleted);
                }
            }
            
            return Result.success(tasks);
        } catch (Exception e) {
            logger.error("获取教师 {} 的任务列表失败", teacherId, e);
            return Result.error("获取任务列表失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取任务详情")
    @GetMapping("/detail/{taskId}")
    public Result<Map<String, Object>> getTaskDetail(
            @PathVariable Long taskId,
            @RequestParam Long courseId,
            HttpServletRequest request) {
        logger.info("接收到获取任务详情请求，任务ID: {}, 课程ID: {}", taskId, courseId);
        
        // 从请求中获取用户ID
        Long teacherId = (Long) request.getAttribute("userId");
        if (teacherId == null) {
            logger.error("未获取到有效的教师ID");
            return Result.error("未登录");
        }
        
        try {
            // 查询任务详情
            MaterialTask task = materialTaskMapper.selectById(taskId);
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return Result.error("任务不存在");
            }
            
            // 查询课程信息
            Course course = courseService.getById(courseId);
            if (course == null) {
                logger.error("课程不存在: {}", courseId);
                return Result.error("课程不存在");
            }
            
            // 组装返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("task", task);
            result.put("course", course);
            
            logger.info("成功获取任务详情，任务ID: {}", taskId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取任务详情失败，任务ID: {}", taskId, e);
            return Result.error("获取任务详情失败: " + e.getMessage());
        }
    }
} 