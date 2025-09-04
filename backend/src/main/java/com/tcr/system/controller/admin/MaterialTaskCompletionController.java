package com.tcr.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Material;
import com.tcr.system.entity.MaterialTaskCompletion;
import com.tcr.system.mapper.MaterialMapper;
import com.tcr.system.mapper.MaterialTaskCompletionMapper;
import com.tcr.system.service.MaterialTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/material-task-completion")
@Api(tags = "管理员-任务完成记录管理")
public class MaterialTaskCompletionController {

    private static final Logger log = LoggerFactory.getLogger(MaterialTaskCompletionController.class);
    
    @Autowired
    private MaterialTaskCompletionMapper materialTaskCompletionMapper;
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private MaterialTaskService materialTaskService;
    
    @ApiOperation("检查和修复任务完成状态")
    @GetMapping("/fix-completion-status")
    public Result<Map<String, Object>> fixCompletionStatus() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> fixedRecords = new ArrayList<>();
        int fixedCount = 0;
        
        try {
            // 1. 查找所有带有taskId的材料
            LambdaQueryWrapper<Material> materialQuery = new LambdaQueryWrapper<>();
            materialQuery.isNotNull(Material::getTaskId)
                         .eq(Material::getDeleted, 0);
            List<Material> materialsWithTask = materialMapper.selectList(materialQuery);
            log.info("找到带有任务ID的材料数量: {}", materialsWithTask.size());
            result.put("totalMaterialsWithTaskId", materialsWithTask.size());
            
            // 2. 检查每个材料是否有对应的任务完成记录
            for (Material material : materialsWithTask) {
                Long taskId = material.getTaskId();
                Long teacherId = material.getUploadUserId();
                Long courseId = material.getCourseId();
                Long materialId = material.getId();
                
                if (taskId == null || teacherId == null || courseId == null) {
                    log.warn("材料缺少必要信息：材料ID={}, 任务ID={}, 教师ID={}, 课程ID={}", 
                        materialId, taskId, teacherId, courseId);
                    continue;
                }
                
                // 检查是否已存在完成记录
                LambdaQueryWrapper<MaterialTaskCompletion> completionQuery = new LambdaQueryWrapper<>();
                completionQuery.eq(MaterialTaskCompletion::getTaskId, taskId)
                              .eq(MaterialTaskCompletion::getTeacherId, teacherId)
                              .eq(MaterialTaskCompletion::getCourseId, courseId)
                              .eq(MaterialTaskCompletion::getDeleted, 0);
                MaterialTaskCompletion existingCompletion = materialTaskCompletionMapper.selectOne(completionQuery);
                
                if (existingCompletion == null) {
                    // 不存在完成记录，创建新记录
                    MaterialTaskCompletion newCompletion = new MaterialTaskCompletion();
                    newCompletion.setTaskId(taskId);
                    newCompletion.setTeacherId(teacherId);
                    newCompletion.setCourseId(courseId);
                    newCompletion.setMaterialId(materialId);
                    newCompletion.setCompletionTime(material.getUpdateTime() != null ? 
                                                   material.getUpdateTime() : LocalDateTime.now());
                    
                    materialTaskCompletionMapper.insert(newCompletion);
                    fixedCount++;
                    
                    Map<String, Object> fixedRecord = new HashMap<>();
                    fixedRecord.put("materialId", materialId);
                    fixedRecord.put("taskId", taskId);
                    fixedRecord.put("teacherId", teacherId);
                    fixedRecord.put("courseId", courseId);
                    fixedRecord.put("completionId", newCompletion.getId());
                    fixedRecords.add(fixedRecord);
                    
                    log.info("已创建缺失的任务完成记录: 材料ID={}, 任务ID={}, 教师ID={}, 课程ID={}, 完成记录ID={}", 
                        materialId, taskId, teacherId, courseId, newCompletion.getId());
                }
            }
            
            result.put("fixedCount", fixedCount);
            result.put("fixedRecords", fixedRecords);
            
            log.info("任务完成状态修复完成，共修复 {} 条记录", fixedCount);
            return Result.success(result);
        } catch (Exception e) {
            log.error("修复任务完成状态失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("查询所有任务完成记录")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listAllCompletions() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<MaterialTaskCompletion> query = new LambdaQueryWrapper<>();
            query.eq(MaterialTaskCompletion::getDeleted, 0)
                 .orderByDesc(MaterialTaskCompletion::getCompletionTime);
            
            List<MaterialTaskCompletion> completions = materialTaskCompletionMapper.selectList(query);
            
            for (MaterialTaskCompletion completion : completions) {
                Map<String, Object> completionMap = new HashMap<>();
                completionMap.put("id", completion.getId());
                completionMap.put("taskId", completion.getTaskId());
                completionMap.put("teacherId", completion.getTeacherId());
                completionMap.put("courseId", completion.getCourseId());
                completionMap.put("materialId", completion.getMaterialId());
                completionMap.put("completionTime", completion.getCompletionTime());
                
                result.add(completionMap);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询任务完成记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("删除任务完成记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCompletion(@PathVariable Long id) {
        try {
            boolean success = materialTaskCompletionMapper.deleteById(id) > 0;
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除任务完成记录失败，ID: {}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
} 