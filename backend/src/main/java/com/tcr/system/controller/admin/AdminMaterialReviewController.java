package com.tcr.system.controller.admin;

import com.tcr.system.entity.MaterialReview;
import com.tcr.system.entity.MaterialReviewRecord;
import com.tcr.system.entity.TempMaterial;
import com.tcr.system.entity.Material;
import com.tcr.system.entity.MaterialTaskCompletion;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.service.MaterialReviewService;
import com.tcr.system.service.MaterialTaskService;
import com.tcr.system.mapper.MaterialReviewRecordMapper;
import com.tcr.system.mapper.TempMaterialMapper;
import com.tcr.system.mapper.MaterialMapper;
import com.tcr.system.mapper.MaterialTaskCompletionMapper;
import com.tcr.system.mapper.MaterialTaskMapper;
import com.tcr.system.common.Result;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/material-review")
public class AdminMaterialReviewController {

    private static final Logger log = LoggerFactory.getLogger(AdminMaterialReviewController.class);

    @Autowired
    private MaterialReviewService materialReviewService;

    @Autowired
    private MaterialReviewRecordMapper materialReviewRecordMapper;

    @Autowired
    private TempMaterialMapper tempMaterialMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private MaterialTaskCompletionMapper materialTaskCompletionMapper;

    @Autowired
    private MaterialTaskService materialTaskService;

    @Autowired
    private MaterialTaskMapper materialTaskMapper;

    @ApiOperation("审核通过")
    @PostMapping("/pass/{reviewId}")
    public Result pass(@PathVariable Long reviewId, @RequestBody MaterialReviewRecord record) {
        try {
            MaterialReview review = materialReviewService.getById(reviewId);
            if (review == null) {
                return Result.error("审核记录不存在");
            }
            
            // 设置审核记录
            record.setReviewId(reviewId);
            record.setResult(1); // 通过
            
            // 保存审核记录
            materialReviewRecordMapper.insert(record);
            
            // 更新审核状态为通过
            review.setStatus(5); // 审核通过
            materialReviewService.updateById(review);
            
            // 将临时材料转为正式材料
            TempMaterial tempMaterial = tempMaterialMapper.selectById(review.getMaterialId());
            if (tempMaterial == null) {
                return Result.error("材料不存在");
            }
            
            Material material = new Material();
            material.setName(tempMaterial.getName());
            material.setDescription(tempMaterial.getDescription());
            material.setCourseId(tempMaterial.getCourseId());
            material.setFilePath(tempMaterial.getFilePath());
            material.setFileSize(tempMaterial.getFileSize());
            material.setFileType(tempMaterial.getFileType());
            material.setUploadUserId(tempMaterial.getUploadUserId());
            material.setDownloadCount(0);
            material.setType(tempMaterial.getType());
            material.setAcademicYear(tempMaterial.getAcademicYear());
            material.setSemester(tempMaterial.getSemester());
            material.setMaterialTypeId(tempMaterial.getMaterialTypeId());
            material.setTaskId(tempMaterial.getTaskId()); // 复制任务ID
            
            materialMapper.insert(material);
            
            // 标记临时材料为已删除
            tempMaterial.setDeleted(true);
            tempMaterialMapper.updateById(tempMaterial);
            
            // 如果是任务提交的材料，记录任务完成情况
            if (tempMaterial.getTaskId() != null) {
                try {
                    // 查询任务信息以获取更多上下文
                    MaterialTask task = materialTaskService.getTaskById(tempMaterial.getTaskId());
                    if (task == null) {
                        // 任务不存在，记录日志但不中断审核流程
                        log.warn("任务ID {} 不存在，无法记录完成情况，但审核仍继续", tempMaterial.getTaskId());
                    } else {
                        // 检查任务状态
                        if (task.getStatus() != 1) { // 1表示进行中的任务
                            log.warn("任务ID {} 不是进行中状态，但仍记录完成情况", tempMaterial.getTaskId());
                        }
                        
                        // 检查任务是否已过期
                        if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDateTime.now())) {
                            log.warn("任务ID {} 已过期，但仍记录完成情况", tempMaterial.getTaskId());
                        }
                    }
                    
                    // 检查该教师是否已经为该任务+课程组合提交过材料
                    Long existingCompletionId = checkExistingCompletion(
                        tempMaterial.getTaskId(),
                        tempMaterial.getUploadUserId(),
                        tempMaterial.getCourseId()
                    );
                    
                    if (existingCompletionId != null) {
                        // 已存在完成记录，可以选择更新已有记录
                        MaterialTaskCompletion existingCompletion = materialTaskCompletionMapper.selectById(existingCompletionId);
                        existingCompletion.setMaterialId(material.getId()); // 更新为新的材料ID
                        existingCompletion.setCompletionTime(LocalDateTime.now()); // 更新完成时间
                        materialTaskCompletionMapper.updateById(existingCompletion);
                        log.info("已更新任务完成记录: 任务ID {}, 教师ID {}, 课程ID {}", 
                            tempMaterial.getTaskId(), tempMaterial.getUploadUserId(), tempMaterial.getCourseId());
                    } else {
                        // 创建新的完成记录
                        MaterialTaskCompletion completion = new MaterialTaskCompletion();
                        completion.setTaskId(tempMaterial.getTaskId());
                        completion.setTeacherId(tempMaterial.getUploadUserId());
                        completion.setCourseId(tempMaterial.getCourseId());
                        completion.setMaterialId(material.getId());
                        completion.setCompletionTime(LocalDateTime.now());
                        
                        materialTaskCompletionMapper.insert(completion);
                        log.info("已创建新的任务完成记录: 任务ID {}, 教师ID {}, 课程ID {}", 
                            tempMaterial.getTaskId(), tempMaterial.getUploadUserId(), tempMaterial.getCourseId());
                    }
                } catch (Exception e) {
                    // 记录任务完成情况失败，但不应影响审核通过流程
                    log.error("记录任务完成情况失败: {}", e.getMessage(), e);
                }
            }
            
            return Result.success("审核通过成功");
        } catch (Exception e) {
            log.error("审核通过失败", e);
            return Result.error("审核通过失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查是否已存在任务完成记录
     * @param taskId 任务ID
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 已存在的记录ID，如果不存在则返回null
     */
    private Long checkExistingCompletion(Long taskId, Long teacherId, Long courseId) {
        QueryWrapper<MaterialTaskCompletion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId)
                   .eq("teacher_id", teacherId)
                   .eq("course_id", courseId)
                   .eq("deleted", 0); // 未删除的记录
        
        MaterialTaskCompletion existingCompletion = materialTaskCompletionMapper.selectOne(queryWrapper);
        return existingCompletion != null ? existingCompletion.getId() : null;
    }
} 