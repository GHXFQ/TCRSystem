package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.CourseMajor;
import com.tcr.system.entity.Major;
import com.tcr.system.entity.MaterialReview;
import com.tcr.system.entity.MaterialReviewRecord;
import com.tcr.system.entity.Material;
import com.tcr.system.entity.TempMaterial;
import com.tcr.system.entity.MaterialTask;
import com.tcr.system.entity.MaterialTaskCompletion;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.CourseMajorMapper;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.MajorMapper;
import com.tcr.system.mapper.MaterialMapper;
import com.tcr.system.mapper.MaterialReviewMapper;
import com.tcr.system.mapper.MaterialReviewRecordMapper;
import com.tcr.system.mapper.TempMaterialMapper;
import com.tcr.system.mapper.MaterialTaskCompletionMapper;
import com.tcr.system.mapper.MaterialTaskMapper;
import com.tcr.system.mapper.TeacherTitleMapper;
import com.tcr.system.service.CourseService;
import com.tcr.system.service.MajorService;
import com.tcr.system.service.MaterialReviewService;
import com.tcr.system.service.MaterialTaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 教学材料审核Service实现类
 */
@Service
@RequiredArgsConstructor
public class MaterialReviewServiceImpl extends ServiceImpl<MaterialReviewMapper, MaterialReview> implements MaterialReviewService {

    private static final Logger log = LoggerFactory.getLogger(MaterialReviewServiceImpl.class);

    @Autowired
    private MaterialReviewRecordMapper materialReviewRecordMapper;
    
    @Autowired
    private TeacherTitleMapper teacherTitleMapper;
    
    @Autowired
    private CourseMajorMapper courseMajorMapper;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private TempMaterialMapper tempMaterialMapper;
    
    @Autowired
    private MaterialTaskCompletionMapper materialTaskCompletionMapper;
    
    @Autowired
    private MaterialTaskService materialTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReview(Long materialId, String academicYear, Integer semester, Long materialTypeId, Long teacherId, Long courseId) {
        // 校验参数
        if (materialId == null) {
            throw new BusinessException("材料ID不能为空");
        }
        if (academicYear == null || academicYear.isEmpty()) {
            throw new BusinessException("学年不能为空");
        }
        if (semester == null) {
            throw new BusinessException("学期不能为空");
        }
        if (materialTypeId == null) {
            throw new BusinessException("材料类型不能为空");
        }
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }

        // 检查是否已存在审核记录
        LambdaQueryWrapper<MaterialReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialReview::getMaterialId, materialId)
                   .eq(MaterialReview::getDeleted, 0);
        MaterialReview existingReview = getOne(queryWrapper);
        if (existingReview != null) {
            return existingReview.getId();
        }
        
        // 获取课程信息
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 获取课程所属专业ID
        List<Long> majorIds = courseMajorMapper.getMajorIdsByCourseId(courseId);
        if (majorIds == null || majorIds.isEmpty()) {
            throw new BusinessException("课程未关联专业");
        }
        
        // 获取专业信息
        Major major = majorService.getById(majorIds.get(0));
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        
        // 获取课程负责人ID
        Long courseLeaderId = teacherTitleMapper.getCourseLeaderIdByCourseId(courseId);
        if (courseLeaderId == null) {
            throw new BusinessException("课程负责人不存在");
        }
        
        // 创建审核记录
        MaterialReview materialReview = new MaterialReview();
        materialReview.setMaterialId(materialId);
        materialReview.setAcademicYear(academicYear);
        materialReview.setSemester(semester);
        materialReview.setMaterialTypeId(materialTypeId);
        materialReview.setStatus(1); // 课程负责人审核中
        materialReview.setCurrentReviewerId(courseLeaderId);
        materialReview.setSubmitUserId(teacherId);
        materialReview.setCourseId(courseId);
        materialReview.setMajorId(major.getId());
        materialReview.setCollegeId(major.getCollegeId());
        
        save(materialReview);
        
        return materialReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewMaterial(Long reviewId, Long reviewerId, Integer reviewerTitle, Integer result, String comment) {
        // 校验参数
        if (reviewId == null) {
            throw new BusinessException("审核ID不能为空");
        }
        if (reviewerId == null) {
            throw new BusinessException("审核人ID不能为空");
        }
        if (reviewerTitle == null) {
            throw new BusinessException("审核人职称不能为空");
        }
        if (result == null) {
            throw new BusinessException("审核结果不能为空");
        }
        
        // 获取审核记录
        MaterialReview materialReview = getById(reviewId);
        if (materialReview == null) {
            throw new BusinessException("审核记录不存在");
        }
        
        // 校验审核人权限
        boolean hasPermission = false;
        switch (materialReview.getStatus()) {
            case 1: // 课程负责人审核中
                // 检查是否为该课程的课程负责人
                Long courseLeaderId = teacherTitleMapper.getCourseLeaderIdByCourseId(materialReview.getCourseId());
                hasPermission = reviewerId.equals(courseLeaderId);
                break;
            case 2: // 专业负责人审核中
                // 检查是否为该专业的专业负责人
                Long majorLeaderId = teacherTitleMapper.getMajorLeaderIdByMajorId(materialReview.getMajorId());
                hasPermission = reviewerId.equals(majorLeaderId);
                break;
            case 3: // 副院长审核中
                // 检查是否为副院长
                List<Long> viceDeanIds = teacherTitleMapper.getViceDeanIdsByCollegeId(materialReview.getCollegeId());
                hasPermission = viceDeanIds != null && viceDeanIds.contains(reviewerId);
                break;
            case 4: // 教务人员审核中
                // 检查是否为管理员
                hasPermission = reviewerId.equals(1L); // 这里暂时使用管理员ID 1
                break;
            default:
                throw new BusinessException("当前状态不允许审核");
        }
        
        if (!hasPermission) {
            throw new BusinessException("您没有权限审核此材料");
        }
        
        // 创建审核记录
        MaterialReviewRecord record = new MaterialReviewRecord();
        record.setReviewId(reviewId);
        record.setReviewerId(reviewerId);
        record.setReviewerTitle(reviewerTitle);
        record.setResult(result);
        record.setComment(comment);
        
        materialReviewRecordMapper.insert(record);
        
        // 更新审核状态
        if (result == 0) {
            // 审核不通过，打回给教师
            materialReview.setStatus(6); // 审核不通过
            materialReview.setCurrentReviewerId(materialReview.getSubmitUserId());
        } else {
            // 审核通过，进入下一环节
            switch (materialReview.getStatus()) {
                case 1: // 课程负责人审核中
                    // 获取专业负责人ID
                    Long majorLeaderId = teacherTitleMapper.getMajorLeaderIdByMajorId(materialReview.getMajorId());
                    if (majorLeaderId == null) {
                        throw new BusinessException("专业负责人不存在");
                    }
                    materialReview.setStatus(2); // 专业负责人审核中
                    materialReview.setCurrentReviewerId(majorLeaderId);
                    break;
                case 2: // 专业负责人审核中
                    // 获取副院长ID
                    Long viceDeanId = teacherTitleMapper.getViceDeanIdByCollegeId(materialReview.getCollegeId());
                    if (viceDeanId == null) {
                        throw new BusinessException("副院长不存在");
                    }
                    materialReview.setStatus(3); // 副院长审核中
                    materialReview.setCurrentReviewerId(viceDeanId);
                    break;
                case 3: // 副院长审核中
                    // 获取教务人员ID，这里暂时使用管理员ID 1
                    Long adminId = 1L;
                    materialReview.setStatus(4); // 教务人员审核中
                    materialReview.setCurrentReviewerId(adminId);
                    break;
                case 4: // 教务人员审核中
                    materialReview.setStatus(5); // 审核通过
                    materialReview.setCurrentReviewerId(null);
                    
                    // 从临时表移动到正式表
                    TempMaterial tempMaterial = tempMaterialMapper.selectById(materialReview.getMaterialId());
                    if (tempMaterial != null) {
                        Material material = new Material();
                        material.setId(tempMaterial.getId()); // 使用相同的ID
                        material.setName(tempMaterial.getName());
                        material.setDescription(tempMaterial.getDescription());
                        material.setCourseId(tempMaterial.getCourseId());
                        material.setFilePath(tempMaterial.getFilePath());
                        material.setFileSize(tempMaterial.getFileSize());
                        material.setFileType(tempMaterial.getFileType());
                        material.setUploadUserId(tempMaterial.getUploadUserId());
                        material.setType(tempMaterial.getType());
                        material.setDownloadCount(0);
                        
                        // 添加从审核记录中获取的学年、学期和材料类型ID
                        material.setAcademicYear(materialReview.getAcademicYear());
                        material.setSemester(materialReview.getSemester());
                        material.setMaterialTypeId(materialReview.getMaterialTypeId());
                        
                        // 复制任务ID
                        material.setTaskId(tempMaterial.getTaskId());
                        
                        materialMapper.insert(material);
                        
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
                                QueryWrapper<MaterialTaskCompletion> queryWrapper = new QueryWrapper<>();
                                queryWrapper.eq("task_id", tempMaterial.getTaskId())
                                           .eq("teacher_id", tempMaterial.getUploadUserId())
                                           .eq("course_id", tempMaterial.getCourseId())
                                           .eq("deleted", 0); // 未删除的记录
                                
                                MaterialTaskCompletion existingCompletion = materialTaskCompletionMapper.selectOne(queryWrapper);
                                
                                if (existingCompletion != null) {
                                    // 已存在完成记录，可以选择更新已有记录
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
                        
                        // 删除临时材料
                        tempMaterialMapper.deleteById(tempMaterial.getId());
                    }
                    break;
                default:
                    throw new BusinessException("审核状态错误");
            }
        }
        
        return updateById(materialReview);
    }

    @Override
    public IPage<Map<String, Object>> getTeacherSubmitReviews(Page<Map<String, Object>> page, Long teacherId, String academicYear, Integer semester, Integer status) {
        return baseMapper.getTeacherSubmitReviews(page, teacherId, academicYear, semester, status);
    }

    @Override
    public IPage<Map<String, Object>> getTeacherPendingReviews(Page<Map<String, Object>> page, Long reviewerId, String academicYear, Integer semester) {
        return baseMapper.getTeacherPendingReviews(page, reviewerId, academicYear, semester);
    }

    @Override
    public Map<String, Object> getReviewDetail(Long reviewId) {
        return baseMapper.getReviewDetail(reviewId);
    }

    @Override
    public List<Map<String, Object>> getReviewRecords(Long reviewId) {
        return materialReviewRecordMapper.getReviewRecords(reviewId);
    }

    @Override
    public IPage<Map<String, Object>> getAdminReviewList(Page<Map<String, Object>> page, String academicYear, Integer semester, Integer status) {
        return baseMapper.getAdminReviewList(page, academicYear, semester, status);
    }
} 