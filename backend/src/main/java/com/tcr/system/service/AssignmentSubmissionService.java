package com.tcr.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcr.system.common.Result;
import com.tcr.system.entity.AssignmentSubmission;
import com.tcr.system.vo.AssignmentSubmissionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 作业提交Service接口
 */
public interface AssignmentSubmissionService extends IService<AssignmentSubmission> {

    /**
     * 获取作业提交列表
     * @param page 分页参数
     * @param assignmentId 作业ID
     * @param status 状态
     * @return 分页结果
     */
    Page<AssignmentSubmissionVO> getSubmissionList(Page<AssignmentSubmission> page, Long assignmentId, Integer status);
    
    /**
     * 获取学生提交的作业列表
     * @param page 分页参数
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 分页结果
     */
    Page<AssignmentSubmissionVO> getStudentSubmissions(Page<AssignmentSubmission> page, Long studentId, Long courseId);
    
    /**
     * 获取作业提交详情
     * @param id 提交ID
     * @return 详情信息
     */
    AssignmentSubmissionVO getSubmissionDetail(Long id);
    
    /**
     * 提交作业
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param content 提交内容
     * @param file 文件
     * @return 提交ID
     */
    Long submitAssignment(Long assignmentId, Long studentId, String content, MultipartFile file);
    
    /**
     * 使用已上传的文件路径提交作业
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param content 提交内容
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 成功结果
     */
    Result<Long> submitWithFilePath(Long assignmentId, Long studentId, String content, String filePath, String fileName);
    
    /**
     * 批改作业
     * @param id 提交ID
     * @param score 得分
     * @param comment 评语
     * @return 是否成功
     */
    boolean gradeSubmission(Long id, Integer score, String comment);
    
    /**
     * 统计作业提交情况
     * @param assignmentId 作业ID
     * @return 统计数据
     */
    Map<String, Object> getSubmissionStatistics(Long assignmentId);
    
    /**
     * 获取学生作业完成率
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 完成率
     */
    Map<String, Object> getStudentCompletionRate(Long courseId, Long studentId);
    
    /**
     * 保存文件
     * @param file 上传的文件
     * @return 文件相对路径
     * @throws java.io.IOException IO异常
     */
    String saveFile(MultipartFile file) throws java.io.IOException;
} 