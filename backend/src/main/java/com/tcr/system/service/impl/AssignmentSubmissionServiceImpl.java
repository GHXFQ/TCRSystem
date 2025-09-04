package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.common.Result;
import com.tcr.system.entity.Assignment;
import com.tcr.system.entity.AssignmentSubmission;
import com.tcr.system.entity.Course;
import com.tcr.system.entity.StudentCourse;
import com.tcr.system.entity.User;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.AssignmentMapper;
import com.tcr.system.mapper.AssignmentSubmissionMapper;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.StudentCourseMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.AssignmentSubmissionService;
import com.tcr.system.vo.AssignmentSubmissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 作业提交Service实现类
 */
@Service
@RequiredArgsConstructor
public class AssignmentSubmissionServiceImpl extends ServiceImpl<AssignmentSubmissionMapper, AssignmentSubmission> implements AssignmentSubmissionService {

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final StudentCourseMapper studentCourseMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public Page<AssignmentSubmissionVO> getSubmissionList(Page<AssignmentSubmission> page, Long assignmentId, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        
        if (status != null) {
            wrapper.eq(AssignmentSubmission::getStatus, status);
        }
        
        wrapper.eq(AssignmentSubmission::getDeleted, 0);
        wrapper.orderByDesc(AssignmentSubmission::getCreateTime);
        
        // 执行查询
        Page<AssignmentSubmission> submissionPage = page(page, wrapper);
        
        // 创建返回结果
        Page<AssignmentSubmissionVO> resultPage = new Page<>();
        BeanUtils.copyProperties(submissionPage, resultPage, "records");
        
        // 处理记录
        List<AssignmentSubmissionVO> records = new ArrayList<>();
        if (submissionPage.getRecords() != null && !submissionPage.getRecords().isEmpty()) {
            // 获取作业信息
            Assignment assignment = assignmentMapper.selectById(assignmentId);
            
            // 获取课程信息，用于设置课程名称
            String courseName = "";
            if (assignment != null && assignment.getCourseId() != null) {
                Course course = courseMapper.selectById(assignment.getCourseId());
                if (course != null) {
                    courseName = course.getName();
                }
            }
            
            for (AssignmentSubmission submission : submissionPage.getRecords()) {
                AssignmentSubmissionVO vo = convertToVO(submission);
                
                // 设置作业标题
                if (assignment != null) {
                    vo.setAssignmentTitle(assignment.getTitle());
                }
                
                // 设置课程名称
                vo.setCourseName(courseName);
                
                // 获取学生信息
                if (submission.getStudentId() != null) {
                    User student = userMapper.selectById(submission.getStudentId());
                    if (student != null) {
                        vo.setStudentName(student.getRealName());
                    }
                }
                
                // 设置文件名
                if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                    String fileName = submission.getFilePath().substring(submission.getFilePath().lastIndexOf("/") + 1);
                    vo.setFileName(fileName);
                }
                
                // 检查是否逾期提交
                if (assignment != null && assignment.getDeadline() != null && submission.getCreateTime() != null) {
                    vo.setIsOverdue(submission.getCreateTime().isAfter(assignment.getDeadline()));
                }
                
                records.add(vo);
            }
        }
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<AssignmentSubmissionVO> getStudentSubmissions(Page<AssignmentSubmission> page, Long studentId, Long courseId) {
        // 创建查询条件
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getStudentId, studentId);
        wrapper.eq(AssignmentSubmission::getDeleted, 0);
        
        // 如果指定了课程ID，则需要关联作业表查询
        if (courseId != null) {
            // 先获取课程下的作业ID列表
            LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
            assignmentWrapper.eq(Assignment::getCourseId, courseId);
            assignmentWrapper.select(Assignment::getId);
            
            List<Assignment> assignments = assignmentMapper.selectList(assignmentWrapper);
            if (assignments.isEmpty()) {
                Page<AssignmentSubmissionVO> emptyPage = new Page<>();
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            
            List<Long> assignmentIds = new ArrayList<>();
            for (Assignment assignment : assignments) {
                assignmentIds.add(assignment.getId());
            }
            
            wrapper.in(AssignmentSubmission::getAssignmentId, assignmentIds);
        }
        
        wrapper.orderByDesc(AssignmentSubmission::getCreateTime);
        
        // 执行查询
        Page<AssignmentSubmission> submissionPage = page(page, wrapper);
        
        // 创建返回结果
        Page<AssignmentSubmissionVO> resultPage = new Page<>();
        BeanUtils.copyProperties(submissionPage, resultPage, "records");
        
        // 处理记录
        List<AssignmentSubmissionVO> records = new ArrayList<>();
        if (submissionPage.getRecords() != null && !submissionPage.getRecords().isEmpty()) {
            for (AssignmentSubmission submission : submissionPage.getRecords()) {
                AssignmentSubmissionVO vo = convertToVO(submission);
                
                // 获取作业信息
                if (submission.getAssignmentId() != null) {
                    Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
                    if (assignment != null) {
                        vo.setAssignmentTitle(assignment.getTitle());
                        
                        // 获取课程信息
                        if (assignment.getCourseId() != null) {
                            Course course = courseMapper.selectById(assignment.getCourseId());
                            if (course != null) {
                                vo.setCourseName(course.getName());
                            }
                        }
                        
                        // 检查是否逾期提交
                        if (assignment.getDeadline() != null && submission.getCreateTime() != null) {
                            vo.setIsOverdue(submission.getCreateTime().isAfter(assignment.getDeadline()));
                        }
                    }
                }
                
                // 设置文件名
                if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                    String fileName = submission.getFilePath().substring(submission.getFilePath().lastIndexOf("/") + 1);
                    vo.setFileName(fileName);
                }
                
                records.add(vo);
            }
        }
        
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAssignment(Long assignmentId, Long studentId, String content, MultipartFile file) {
        // 验证参数
        if (assignmentId == null) {
            throw new BusinessException("作业ID不能为空");
        }
        
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        // 查询作业信息
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        
        // 检查是否已经提交过
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        wrapper.eq(AssignmentSubmission::getStudentId, studentId);
        wrapper.eq(AssignmentSubmission::getDeleted, 0);
        
        AssignmentSubmission existingSubmission = getOne(wrapper);
        
        if (existingSubmission != null) {
            // 已经提交过，执行更新
            existingSubmission.setContent(content);
            
            // 处理文件上传
            if (file != null && !file.isEmpty()) {
                try {
                    String filePath = saveFile(file);
                    existingSubmission.setFilePath(filePath);
                    existingSubmission.setFileSize(file.getSize());
                    existingSubmission.setFileType(file.getContentType());
                } catch (IOException e) {
                    throw new BusinessException("文件上传失败：" + e.getMessage());
                }
            }
            
            updateById(existingSubmission);
            return existingSubmission.getId();
        } else {
            // 新提交
            AssignmentSubmission submission = new AssignmentSubmission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(content);
            submission.setStatus(0); // 未批改
            
            // 处理文件上传
            if (file != null && !file.isEmpty()) {
                try {
                    String filePath = saveFile(file);
                    submission.setFilePath(filePath);
                    submission.setFileSize(file.getSize());
                    submission.setFileType(file.getContentType());
                } catch (IOException e) {
                    throw new BusinessException("文件上传失败：" + e.getMessage());
                }
            }
            
            save(submission);
            return submission.getId();
        }
    }

    @Override
    public boolean gradeSubmission(Long id, Integer score, String comment) {
        // 验证参数
        if (id == null) {
            throw new BusinessException("提交ID不能为空");
        }
        
        if (score == null) {
            throw new BusinessException("得分不能为空");
        }
        
        // 查询提交信息
        AssignmentSubmission submission = getById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        
        // 更新批改信息
        submission.setScore(score);
        submission.setComment(comment);
        submission.setStatus(1); // 已批改
        
        return updateById(submission);
    }

    @Override
    public AssignmentSubmissionVO getSubmissionDetail(Long id) {
        // 查询提交信息
        AssignmentSubmission submission = getById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        
        AssignmentSubmissionVO vo = convertToVO(submission);
        
        // 获取作业信息
        if (submission.getAssignmentId() != null) {
            Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
            if (assignment != null) {
                vo.setAssignmentTitle(assignment.getTitle());
                
                // 检查是否逾期提交
                if (assignment.getDeadline() != null && submission.getCreateTime() != null) {
                    vo.setIsOverdue(submission.getCreateTime().isAfter(assignment.getDeadline()));
                }
            }
        }
        
        // 获取学生信息
        if (submission.getStudentId() != null) {
            User student = userMapper.selectById(submission.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
            }
            }
            
            // 设置文件名
        if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
            String fileName = submission.getFilePath().substring(submission.getFilePath().lastIndexOf("/") + 1);
            vo.setFileName(fileName);
        }
        
        return vo;
    }

    @Override
    public Map<String, Object> getSubmissionStatistics(Long assignmentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询作业信息
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        
        // 查询所有提交记录
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        wrapper.eq(AssignmentSubmission::getDeleted, 0);
        
        List<AssignmentSubmission> submissions = list(wrapper);
        int totalSubmissions = submissions.size();
        
        // 统计已批改数量
        int gradedCount = 0;
        int totalScore = 0;
        int passCount = 0; // 分数及格的数量（60分以上）
        
        for (AssignmentSubmission submission : submissions) {
            if (submission.getStatus() != null && submission.getStatus() == 1) {
                gradedCount++;
                
                if (submission.getScore() != null) {
                    totalScore += submission.getScore();
                    
                    if (submission.getScore() >= 60) {
                        passCount++;
                    }
                }
            }
        }
        
        // 计算平均分
        double avgScore = 0;
        if (gradedCount > 0) {
            avgScore = (double) totalScore / gradedCount;
        }
        
        // 获取课程信息
        String courseName = "";
        Long courseId = null;
        if (assignment.getCourseId() != null) {
            courseId = assignment.getCourseId();
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                courseName = course.getName();
            }
        }
        
        // 获取选课学生总数
        int totalStudents = 0;
        if (courseId != null) {
            // 使用StudentCourseMapper查询选课学生数量
            LambdaQueryWrapper<StudentCourse> studentCourseWrapper = new LambdaQueryWrapper<>();
            studentCourseWrapper.eq(StudentCourse::getCourseId, courseId);
            studentCourseWrapper.eq(StudentCourse::getDeleted, 0);
            totalStudents = Math.toIntExact(studentCourseMapper.selectCount(studentCourseWrapper));
        }
        
        // 构建结果
        result.put("assignmentId", assignmentId);
        result.put("title", assignment.getTitle());
        result.put("courseName", courseName);
        result.put("totalSubmissions", totalSubmissions);
        result.put("gradedCount", gradedCount);
        result.put("notGradedCount", totalSubmissions - gradedCount);
        result.put("avgScore", String.format("%.1f", avgScore));
        result.put("totalStudents", totalStudents);
        result.put("submittedCount", totalSubmissions);
        
        // 计算提交率
        double submissionRate = 0;
        if (totalStudents > 0) {
            submissionRate = (double) totalSubmissions / totalStudents * 100;
        }
        result.put("submissionRate", String.format("%.2f", submissionRate) + "%");
        
        // 计算及格率
        double passRate = 0;
        if (gradedCount > 0) {
            passRate = (double) passCount / gradedCount * 100;
        }
        result.put("passRate", String.format("%.2f", passRate) + "%");
        
        // 批改率
        double gradedRate = 0;
        if (totalSubmissions > 0) {
            gradedRate = (double) gradedCount / totalSubmissions * 100;
        }
        result.put("gradedRate", String.format("%.2f", gradedRate) + "%");
        
        // 将gradedRate添加为gradingRate，保持与前端一致
        result.put("gradingRate", String.format("%.2f", gradedRate) + "%");
        
        // 截止日期
        if (assignment.getDeadline() != null) {
            result.put("deadline", assignment.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            result.put("deadline", "--");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getStudentCompletionRate(Long courseId, Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询课程下的所有作业
        LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
        assignmentWrapper.eq(Assignment::getCourseId, courseId);
        assignmentWrapper.eq(Assignment::getDeleted, 0);
        
        List<Assignment> assignments = assignmentMapper.selectList(assignmentWrapper);
        int totalAssignments = assignments.size();
        
        // 查询学生已提交的作业
        List<Long> assignmentIds = new ArrayList<>();
        for (Assignment assignment : assignments) {
            assignmentIds.add(assignment.getId());
        }
        
        if (assignmentIds.isEmpty()) {
            result.put("courseId", courseId);
            result.put("totalAssignments", 0);
            result.put("completedCount", 0);
            result.put("completionRate", "0%");
            result.put("avgScore", "--");
            return result;
        }
        
        LambdaQueryWrapper<AssignmentSubmission> submissionWrapper = new LambdaQueryWrapper<>();
        submissionWrapper.eq(AssignmentSubmission::getStudentId, studentId);
        submissionWrapper.in(AssignmentSubmission::getAssignmentId, assignmentIds);
        submissionWrapper.eq(AssignmentSubmission::getDeleted, 0);
        
        List<AssignmentSubmission> submissions = list(submissionWrapper);
        int completedCount = submissions.size();
        
        // 计算完成率
        double completionRate = 0;
        if (totalAssignments > 0) {
            completionRate = (double) completedCount / totalAssignments * 100;
        }
        
        // 计算平均分
        int totalScore = 0;
        int gradedCount = 0;
        
        for (AssignmentSubmission submission : submissions) {
            if (submission.getStatus() != null && submission.getStatus() == 1 && submission.getScore() != null) {
                totalScore += submission.getScore();
                gradedCount++;
            }
        }
        
        double avgScore = 0;
        if (gradedCount > 0) {
            avgScore = (double) totalScore / gradedCount;
        }
        
        // 构建结果
        result.put("courseId", courseId);
        result.put("totalAssignments", totalAssignments);
        result.put("completedCount", completedCount);
        result.put("completionRate", String.format("%.2f", completionRate) + "%");
        
        if (gradedCount > 0) {
            result.put("avgScore", String.format("%.1f", avgScore));
        } else {
            result.put("avgScore", "--");
        }
        
        return result;
    }

    @Override
    public Result<Long> submitWithFilePath(Long assignmentId, Long studentId, String content, String filePath, String fileName) {
        try {
            // 查询作业信息
            Assignment assignment = assignmentMapper.selectById(assignmentId);
            if (assignment == null) {
                return Result.error("作业不存在");
            }
            
            // 获取文件信息
            Path path = null;
            if (filePath.startsWith("/uploads/")) {
                String relativePath = filePath.substring("/uploads/".length());
                path = Paths.get(uploadDir).resolve(relativePath);
            } else {
                path = Paths.get(uploadDir).resolve(filePath);
            }
            
            if (!Files.exists(path)) {
                return Result.error("文件不存在");
            }
            
            String fileType = Files.probeContentType(path);
            long fileSize = Files.size(path);
            
            // 检查是否已经提交过
            LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
            wrapper.eq(AssignmentSubmission::getStudentId, studentId);
            wrapper.eq(AssignmentSubmission::getDeleted, 0);
            
            AssignmentSubmission existingSubmission = getOne(wrapper);
            
            if (existingSubmission != null) {
                // 已经提交过，执行更新
                existingSubmission.setContent(content);
                existingSubmission.setFilePath(filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath);
                existingSubmission.setFileSize(fileSize);
                existingSubmission.setFileType(fileType);
                existingSubmission.setStatus(0); // 重置为未批改状态
                
                updateById(existingSubmission);
                return Result.success(existingSubmission.getId(), "提交成功");
            } else {
                // 新提交
                AssignmentSubmission submission = new AssignmentSubmission();
                submission.setAssignmentId(assignmentId);
                submission.setStudentId(studentId);
                submission.setContent(content);
                submission.setFilePath(filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath);
                submission.setFileSize(fileSize);
                submission.setFileType(fileType);
                submission.setStatus(0); // 未批改
                
                save(submission);
                return Result.success(submission.getId(), "提交成功");
            }
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 将实体转换为VO
     */
    private AssignmentSubmissionVO convertToVO(AssignmentSubmission submission) {
        AssignmentSubmissionVO vo = new AssignmentSubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 保存文件
     */
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path uploadPath = Paths.get(uploadDir, datePath);
        Files.createDirectories(uploadPath);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 返回相对路径
        return datePath + "/" + fileName;
    }
} 