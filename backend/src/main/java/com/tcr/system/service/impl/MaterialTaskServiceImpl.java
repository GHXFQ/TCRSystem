package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Material;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.entity.MaterialTaskCompletion;
import com.tcr.system.entity.User;
import com.tcr.system.mapper.MaterialMapper;
import com.tcr.system.mapper.MaterialTaskCompletionMapper;
import com.tcr.system.mapper.MaterialTaskMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.MaterialTaskService;
import com.tcr.system.vo.MaterialTaskProgressVO;
import com.tcr.system.vo.MaterialTaskReminderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 教学材料任务服务实现类
 */
@Service
public class MaterialTaskServiceImpl extends ServiceImpl<MaterialTaskMapper, MaterialTask> implements MaterialTaskService {

    @Autowired
    private MaterialTaskMapper materialTaskMapper;
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private MaterialTaskCompletionMapper materialTaskCompletionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    private final static Logger log = LoggerFactory.getLogger(MaterialTaskServiceImpl.class);

    @Override
    public IPage<MaterialTask> getTaskPage(Page<MaterialTask> page, Long materialTypeId, String academicYear, Integer semester, Integer status, String title) {
        return materialTaskMapper.selectTaskPage(page, materialTypeId, academicYear, semester, status, title);
    }

    @Override
    public MaterialTask getTaskById(Long id) {
        return getById(id);
    }

    @Override
    public boolean addTask(MaterialTask task) {
        task.setCreateTime(LocalDateTime.now());
        return save(task);
    }

    @Override
    public boolean updateTask(MaterialTask task) {
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    public boolean deleteTask(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteTaskBatch(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean publishTask(Long id) {
        MaterialTask task = getById(id);
        if (task == null) {
            return false;
        }
        task.setStatus(1); // 设置为已发布状态
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    public boolean publishTaskBatch(List<Long> ids) {
        for (Long id : ids) {
            publishTask(id);
        }
        return true;
    }

    @Override
    public MaterialTaskProgressVO getTaskProgress(Long id) {
        // 实现获取任务完成情况的逻辑
        MaterialTaskProgressVO progressVO = new MaterialTaskProgressVO();
        
        try {
            log.info("开始查询任务ID {}的完成情况", id);
            
            // 1. 查询任务基本信息
            MaterialTask task = this.getById(id);
            if (task == null) {
                log.warn("任务ID {}不存在", id);
                return progressVO;
            }
            
            // 2. 查询任务完成统计信息
            Map<String, Object> statsMap = materialTaskCompletionMapper.selectCompletionStatsByTaskId(id);
            if (statsMap != null) {
                // 设置总教师数和已完成教师数
                progressVO.setTotalCount(statsMap.get("total_count") != null ? 
                    Integer.parseInt(statsMap.get("total_count").toString()) : 0);
                progressVO.setCompletedCount(statsMap.get("completed_count") != null ? 
                    Integer.parseInt(statsMap.get("completed_count").toString()) : 0);
            }
            
            // 3. 查询已完成的教师列表
            Page<MaterialTaskCompletion> completedPage = new Page<>(1, 100); // 设置较大的页码，一般不会超过100个
            IPage<MaterialTaskCompletion> completedResult = materialTaskCompletionMapper.selectCompletionsByTaskId(completedPage, id);
            
            List<Map<String, Object>> completedTeachers = new ArrayList<>();
            if (completedResult != null && completedResult.getRecords() != null) {
                for (MaterialTaskCompletion completion : completedResult.getRecords()) {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("teacherId", completion.getTeacherId());
                    teacherMap.put("teacherName", completion.getTeacherName());
                    teacherMap.put("courseId", completion.getCourseId());
                    teacherMap.put("courseName", completion.getCourseName());
                    teacherMap.put("materialId", completion.getMaterialId());
                    teacherMap.put("materialName", completion.getMaterialName());
                    teacherMap.put("submitTime", completion.getCompletionTime());
                    completedTeachers.add(teacherMap);
                }
            }
            progressVO.setCompletedTeachers(completedTeachers);
            
            // 4. 查询未完成的教师列表
            List<Map<String, Object>> uncompletedTeachers = materialTaskCompletionMapper.selectUncompletedTeachersByTaskId(id);
            
            // 处理未完成教师列表，将字段名从下划线格式转换为驼峰格式
            if (uncompletedTeachers != null) {
                List<Map<String, Object>> processedTeachers = new ArrayList<>();
                for (Map<String, Object> teacher : uncompletedTeachers) {
                    Map<String, Object> processedTeacher = new HashMap<>();
                    
                    // 转换字段名
                    if (teacher.containsKey("teacher_id")) {
                        processedTeacher.put("teacherId", teacher.get("teacher_id"));
                    } else {
                        processedTeacher.put("teacherId", "");
                    }
                    
                    if (teacher.containsKey("teacher_name")) {
                        processedTeacher.put("teacherName", teacher.get("teacher_name"));
                    } else {
                        processedTeacher.put("teacherName", "");
                    }
                    
                    if (teacher.containsKey("course_id")) {
                        processedTeacher.put("courseId", teacher.get("course_id"));
                    } else {
                        processedTeacher.put("courseId", "");
                    }
                    
                    if (teacher.containsKey("course_name")) {
                        processedTeacher.put("courseName", teacher.get("course_name"));
                    } else {
                        processedTeacher.put("courseName", "");
                    }
                    
                    // 保留其他字段
                    if (teacher.containsKey("email")) {
                        processedTeacher.put("email", teacher.get("email"));
                    } else {
                        processedTeacher.put("email", "");
                    }
                    
                    if (teacher.containsKey("phone")) {
                        processedTeacher.put("phone", teacher.get("phone"));
                    } else {
                        processedTeacher.put("phone", "");
                    }
                    
                    processedTeachers.add(processedTeacher);
                }
                uncompletedTeachers = processedTeachers;
            }
            
            progressVO.setUncompletedTeachers(uncompletedTeachers != null ? uncompletedTeachers : new ArrayList<>());
            
            log.info("任务ID {}的完成情况查询结果: 总数={}, 已完成={}, 已完成教师数={}, 未完成教师数={}", 
                id, progressVO.getTotalCount(), progressVO.getCompletedCount(), 
                completedTeachers.size(), progressVO.getUncompletedTeachers().size());
        } catch (Exception e) {
            log.error("查询任务完成情况出错", e);
        }
        
        return progressVO;
    }

    @Override
    public boolean remindTeacher(Long taskId, Long teacherId) {
        // 实现提醒未完成任务的教师的逻辑
        // TODO: 发送提醒通知的实现
        return true;
    }

    @Override
    public boolean remindTeacherBatch(List<MaterialTaskReminderVO> reminders) {
        for (MaterialTaskReminderVO reminder : reminders) {
            remindTeacher(reminder.getTaskId(), reminder.getTeacherId());
        }
        return true;
    }

    @Override
    public List<MaterialTask> getRecentTasks(int limit) {
        return materialTaskMapper.selectRecentTasks(limit);
    }

    @Override
    public List<Map<String, Object>> getTeacherTasks(Long teacherId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            log.info("开始获取教师[{}]的任务列表", teacherId);
            
            // 1. 查询该教师负责的所有课程
            LambdaQueryWrapper<com.tcr.system.entity.Course> courseQuery = new LambdaQueryWrapper<>();
            courseQuery.eq(com.tcr.system.entity.Course::getTeacherId, teacherId)
                      .eq(com.tcr.system.entity.Course::getDeleted, 0);
            List<com.tcr.system.entity.Course> teacherCourses = baseMapper.selectCoursesByTeacherId(teacherId);
            
            log.info("教师[{}]负责的课程数量: {}", teacherId, teacherCourses.size());
            
            if (teacherCourses.isEmpty()) {
                log.warn("教师[{}]没有负责的课程，返回空任务列表", teacherId);
                return result; // 教师没有课程，返回空列表
            }
            
            // 提取课程ID列表
            List<Long> courseIds = new ArrayList<>();
            for (com.tcr.system.entity.Course course : teacherCourses) {
                courseIds.add(course.getId());
                log.info("教师课程: ID={}, 名称={}", course.getId(), course.getName());
            }
            
            // 2. 查询与这些课程相关的任务（状态为进行中）
            List<MaterialTask> taskList = baseMapper.selectTasksForTeacher(courseIds);
            log.info("找到与教师课程相关的任务数量: {}", taskList.size());
            
            // 打印任务详情进行调试
            for (MaterialTask task : taskList) {
                log.info("任务详情: ID={}, 标题={}, 课程IDs={}", task.getId(), task.getTitle(), task.getCourseIds());
            }
            
            // 3. 查询教师已完成的任务
            LambdaQueryWrapper<MaterialTaskCompletion> completionQuery = new LambdaQueryWrapper<>();
            completionQuery.eq(MaterialTaskCompletion::getTeacherId, teacherId)
                          .eq(MaterialTaskCompletion::getDeleted, 0);
            List<MaterialTaskCompletion> completions = materialTaskCompletionMapper.selectList(completionQuery);
            log.info("教师已完成任务数量: {}", completions.size());
            
            // 构建已完成任务的映射 (taskId_courseId -> completion)
            Map<String, MaterialTaskCompletion> completionMap = new HashMap<>();
            for (MaterialTaskCompletion completion : completions) {
                String key = completion.getTaskId() + "_" + completion.getCourseId();
                completionMap.put(key, completion);
                log.info("已完成任务: 任务ID={}, 课程ID={}", completion.getTaskId(), completion.getCourseId());
            }
            
            // 4. 构建结果列表
            for (MaterialTask task : taskList) {
                for (com.tcr.system.entity.Course course : teacherCourses) {
                    // 检查该课程是否在该任务的课程列表中
                    boolean isInTask = isCoursesInTask(course.getId(), task.getCourseIds());
                    log.info("检查课程[{}]是否在任务[{}]中: {}", course.getId(), task.getId(), isInTask);
                    
                    if (isInTask) {
                        Map<String, Object> taskMap = new HashMap<>();
                        taskMap.put("taskId", task.getId());
                        taskMap.put("title", task.getTitle());
                        taskMap.put("description", task.getDescription());
                        taskMap.put("courseId", course.getId());
                        taskMap.put("courseName", course.getName());
                        taskMap.put("materialTypeId", task.getMaterialTypeId());
                        taskMap.put("materialTypeName", task.getMaterialTypeName());
                        taskMap.put("academicYear", task.getAcademicYear());
                        taskMap.put("semester", task.getSemester());
                        taskMap.put("deadline", task.getDeadline());
                        
                        // 检查是否已完成
                        String completionKey = task.getId() + "_" + course.getId();
                        boolean isCompleted = completionMap.containsKey(completionKey);
                        taskMap.put("completed", isCompleted);
                        log.info("任务[{}]课程[{}]是否已完成: {}", task.getId(), course.getId(), isCompleted);
                        
                        // 添加到结果列表
                        result.add(taskMap);
                    }
                }
            }
            
            log.info("最终返回的任务数量: {}", result.size());
            
        } catch (Exception e) {
            log.error("获取教师任务列表出错：", e);
        }
        
        return result;
    }
    
    /**
     * 检查课程是否在任务的课程列表中
     * @param courseId 课程ID
     * @param courseIdsStr 任务的课程ID字符串（逗号分隔）
     * @return 课程是否在列表中
     */
    private boolean isCoursesInTask(Long courseId, String courseIdsStr) {
        if (courseId == null || courseIdsStr == null || courseIdsStr.isEmpty()) {
            return false;
        }
        
        String[] courseIdArray = courseIdsStr.split(",");
        for (String idStr : courseIdArray) {
            if (idStr.trim().equals(courseId.toString())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Map<String, Object> getStatisticsOverview(Long materialTypeId, String academicYear, Integer semester) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询上传材料总数
            LambdaQueryWrapper<Material> materialQuery = new LambdaQueryWrapper<>();
            materialQuery.eq(Material::getDeleted, 0); // 未删除的材料
            
            if (materialTypeId != null) {
                materialQuery.eq(Material::getMaterialTypeId, materialTypeId);
            }
            
            if (academicYear != null && !academicYear.isEmpty()) {
                materialQuery.eq(Material::getAcademicYear, academicYear);
            }
            
            if (semester != null) {
                materialQuery.eq(Material::getSemester, semester);
            }
            
            Long totalMaterialsCount = materialMapper.selectCount(materialQuery);
            result.put("totalMaterials", totalMaterialsCount);
            
            // 2. 查询相关任务
            LambdaQueryWrapper<MaterialTask> taskQuery = new LambdaQueryWrapper<>();
            taskQuery.eq(MaterialTask::getDeleted, 0) // 未删除的任务
                     .eq(MaterialTask::getStatus, 1); // 已发布的任务
                     
            if (materialTypeId != null) {
                taskQuery.eq(MaterialTask::getMaterialTypeId, materialTypeId);
            }
            
            if (academicYear != null && !academicYear.isEmpty()) {
                taskQuery.eq(MaterialTask::getAcademicYear, academicYear);
            }
            
            if (semester != null) {
                taskQuery.eq(MaterialTask::getSemester, semester);
            }
            
            List<MaterialTask> tasks = this.list(taskQuery);
            
            if (tasks.isEmpty()) {
                // 如果没有任务，则设置已完成和未完成教师数都为0
                result.put("completedTeachers", 0);
                result.put("uncompletedTeachers", 0);
                return result;
            }
            
            // 3. 获取任务ID列表和关联的课程ID列表
            List<Long> taskIds = new ArrayList<>();
            Set<Long> relatedCourseIds = new HashSet<>();
            
            for (MaterialTask task : tasks) {
                taskIds.add(task.getId());
                
                // 解析关联课程ID
                if (task.getCourseIds() != null && !task.getCourseIds().isEmpty()) {
                    String[] courseIdArray = task.getCourseIds().split(",");
                    for (String courseIdStr : courseIdArray) {
                        try {
                            Long courseId = Long.parseLong(courseIdStr.trim());
                            relatedCourseIds.add(courseId);
                        } catch (NumberFormatException e) {
                            // 忽略无效的课程ID
                        }
                    }
                }
            }
            
            if (relatedCourseIds.isEmpty()) {
                // 如果没有关联课程，则设置已完成和未完成教师数都为0
                result.put("completedTeachers", 0);
                result.put("uncompletedTeachers", 0);
                return result;
            }
            
            // 4. 获取与任务相关课程的教师列表（去重）
            List<Long> relatedTeacherIds = materialTaskMapper.selectTeacherIdsWithCourses(new ArrayList<>(relatedCourseIds));
            
            if (relatedTeacherIds.isEmpty()) {
                // 如果没有相关教师，则设置已完成和未完成教师数都为0
                result.put("completedTeachers", 0);
                result.put("uncompletedTeachers", 0);
                return result;
            }
            
            // 5. 获取已完成任务的教师ID（去重）
            LambdaQueryWrapper<MaterialTaskCompletion> completionQuery = new LambdaQueryWrapper<>();
            completionQuery.in(MaterialTaskCompletion::getTaskId, taskIds)
                           .eq(MaterialTaskCompletion::getDeleted, 0);
                           
            List<MaterialTaskCompletion> completions = materialTaskCompletionMapper.selectList(completionQuery);
            
            // 统计已完成任务的教师ID（去重）
            Set<Long> completedTeacherIds = new HashSet<>();
            for (MaterialTaskCompletion completion : completions) {
                completedTeacherIds.add(completion.getTeacherId());
            }
            
            // 6. 计算已完成和未完成教师数
            // 已完成的教师数：已完成任务的教师中，与任务相关的教师数
            Set<Long> validCompletedTeacherIds = new HashSet<>();
            for (Long teacherId : completedTeacherIds) {
                if (relatedTeacherIds.contains(teacherId)) {
                    validCompletedTeacherIds.add(teacherId);
                }
            }
            
            int completedCount = validCompletedTeacherIds.size();
            
            // 未完成的教师数：与任务相关的教师总数 - 已完成的教师数
            int uncompletedCount = relatedTeacherIds.size() - completedCount;
            
            // 设置统计结果
            result.put("completedTeachers", completedCount);
            result.put("uncompletedTeachers", uncompletedCount);
            
        } catch (Exception e) {
            log.error("获取统计概览出错：", e);
            // 出现异常时返回默认值
            result.put("totalMaterials", 0);
            result.put("completedTeachers", 0);
            result.put("uncompletedTeachers", 0);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> statisticsByType() {
        List<Map<String, Object>> result = materialTaskMapper.statisticsByType();
        
        // 如果数据为空，返回默认格式以避免前端渲染错误
        if (result == null || result.isEmpty()) {
            result = new ArrayList<>();
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("type_id", 0);
            defaultData.put("type_name", "暂无数据");
            defaultData.put("name", "暂无数据");  // 确保提供name字段
            defaultData.put("completed", 0);
            defaultData.put("uncompleted", 0);
            result.add(defaultData);
        } else {
            // 确保每个项都有name字段，前端echarts需要此字段
            for (Map<String, Object> item : result) {
                if (!item.containsKey("name")) {
                    item.put("name", item.get("type_name"));
                }
            }
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> statisticsByCollege() {
        List<Map<String, Object>> result = materialTaskMapper.statisticsByCollege();
        
        // 如果数据为空，返回默认格式以避免前端渲染错误
        if (result == null || result.isEmpty()) {
            result = new ArrayList<>();
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("college_id", 0);
            defaultData.put("college_name", "暂无数据");
            defaultData.put("name", "暂无数据");  // 确保提供name字段
            defaultData.put("count", 0);
            result.add(defaultData);
        } else {
            // 确保每个项都有name字段，前端echarts需要此字段
            for (Map<String, Object> item : result) {
                if (!item.containsKey("name")) {
                    item.put("name", item.get("college_name"));
                }
            }
        }
        
        return result;
    }

    @Override
    public IPage<Map<String, Object>> getUncompletedTeachersPage(Page<Map<String, Object>> page, Long materialTypeId, String academicYear, Integer semester) {
        try {
            // 1. 查询相关任务
            LambdaQueryWrapper<MaterialTask> taskQuery = new LambdaQueryWrapper<>();
            taskQuery.eq(MaterialTask::getDeleted, 0) // 未删除的任务
                     .eq(MaterialTask::getStatus, 1); // 已发布的任务
                     
            if (materialTypeId != null) {
                taskQuery.eq(MaterialTask::getMaterialTypeId, materialTypeId);
            }
            
            if (academicYear != null && !academicYear.isEmpty()) {
                taskQuery.eq(MaterialTask::getAcademicYear, academicYear);
            }
            
            if (semester != null) {
                taskQuery.eq(MaterialTask::getSemester, semester);
            }
            
            List<MaterialTask> tasks = this.list(taskQuery);
            
            if (tasks.isEmpty()) {
                // 如果没有任务，返回空分页结果
                return page;
            }
            
            // 2. 获取任务ID列表和关联的课程ID列表
            List<Long> taskIds = new ArrayList<>();
            Set<Long> relatedCourseIds = new HashSet<>();
            
            for (MaterialTask task : tasks) {
                taskIds.add(task.getId());
                
                // 解析关联课程ID
                if (task.getCourseIds() != null && !task.getCourseIds().isEmpty()) {
                    String[] courseIdArray = task.getCourseIds().split(",");
                    for (String courseIdStr : courseIdArray) {
                        try {
                            Long courseId = Long.parseLong(courseIdStr.trim());
                            relatedCourseIds.add(courseId);
                        } catch (NumberFormatException e) {
                            // 忽略无效的课程ID
                        }
                    }
                }
            }
            
            if (relatedCourseIds.isEmpty()) {
                // 如果没有关联课程，返回空分页结果
                return page;
            }
            
            // 3. 执行分页查询，获取未完成任务的教师列表
            IPage<Map<String, Object>> result = baseMapper.selectUncompletedTeachersPage(
                page, 
                taskIds, 
                new ArrayList<>(relatedCourseIds),
                materialTypeId,
                academicYear,
                semester
            );
            
            return result;
            
        } catch (Exception e) {
            log.error("获取未完成任务的教师列表出错：", e);
            return page; // 出现异常返回空分页结果
        }
    }
} 