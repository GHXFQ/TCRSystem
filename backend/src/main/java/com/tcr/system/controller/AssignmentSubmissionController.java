package com.tcr.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcr.system.common.Result;
import com.tcr.system.entity.AssignmentSubmission;
import com.tcr.system.service.AssignmentSubmissionService;
import com.tcr.system.vo.AssignmentSubmissionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;

/**
 * 作业提交控制器
 */
@RestController
@RequestMapping("/api/assignment/submission")
@RequiredArgsConstructor
@Api(tags = "作业提交管理")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 分页查询作业提交列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询作业提交列表")
    public Result<Page<AssignmentSubmissionVO>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("作业ID") @RequestParam Long assignmentId,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<AssignmentSubmission> page = new Page<>(pageNum, pageSize);
        Page<AssignmentSubmissionVO> result = submissionService.getSubmissionList(page, assignmentId, status);
        return Result.success(result);
    }

    /**
     * 获取作业提交详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取作业提交详情")
    public Result<AssignmentSubmissionVO> getById(@ApiParam("提交ID") @PathVariable Long id) {
        AssignmentSubmissionVO submission = submissionService.getSubmissionDetail(id);
        return Result.success(submission);
    }

    /**
     * 获取学生的作业提交
     */
    @GetMapping("/student")
    @ApiOperation("获取学生的作业提交")
    public Result<Page<AssignmentSubmissionVO>> getStudentSubmissions(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute("userId") Long userId) {
        Page<AssignmentSubmission> page = new Page<>(pageNum, pageSize);
        Page<AssignmentSubmissionVO> submissions = submissionService.getStudentSubmissions(page, userId, null);
        return Result.success(submissions);
    }

    /**
     * 获取学生在指定课程的作业提交
     */
    @GetMapping("/student/course/{courseId}")
    @ApiOperation("获取学生在指定课程的作业提交")
    public Result<Page<AssignmentSubmissionVO>> getStudentCourseSubmissions(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute("userId") Long userId,
            @ApiParam("课程ID") @PathVariable Long courseId) {
        Page<AssignmentSubmission> page = new Page<>(pageNum, pageSize);
        Page<AssignmentSubmissionVO> submissions = submissionService.getStudentSubmissions(page, userId, courseId);
        return Result.success(submissions);
    }

    /**
     * 获取指定作业的学生提交
     */
    @GetMapping("/assignment/{assignmentId}")
    @ApiOperation("获取指定作业的学生提交")
    public Result<AssignmentSubmissionVO> getSubmission(
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @RequestAttribute("userId") Long userId) {
        // 首先需要查询该学生是否有提交该作业
        Page<AssignmentSubmission> page = new Page<>(1, 1);
        Page<AssignmentSubmissionVO> submissions = submissionService.getSubmissionList(page, assignmentId, null);
        if (submissions.getRecords().isEmpty()) {
            return Result.error("未找到提交记录");
        }
        return Result.success(submissions.getRecords().get(0));
    }
    
    /**
     * 获取作业提交统计信息
     */
    @GetMapping("/statistics/{assignmentId}")
    @ApiOperation("获取作业提交统计信息")
    public Result<Map<String, Object>> getSubmissionStatistics(
            @ApiParam("作业ID") @PathVariable Long assignmentId) {
        Map<String, Object> statistics = submissionService.getSubmissionStatistics(assignmentId);
        return Result.success(statistics);
    }

    /**
     * 提交作业
     */
    @PostMapping
    @ApiOperation("提交作业")
    public Result<Long> submit(
            @ApiParam("作业ID") @RequestParam Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        if (content == null) {
            content = "";
        }
        
        // 处理从上传接口获取的文件路径
        if (file == null && filePath != null && !filePath.isEmpty()) {
            return submitWithFilePath(assignmentId, content, filePath, userId);
        }
        
        Long submissionId = submissionService.submitAssignment(assignmentId, userId, content, file);
        return Result.success(submissionId, "提交成功");
    }
    
    /**
     * 使用已上传的文件路径提交作业
     */
    private Result<Long> submitWithFilePath(Long assignmentId, String content, String filePath, Long userId) {
        try {
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
            
            String fileName = path.getFileName().toString();
            String fileType = Files.probeContentType(path);
            long fileSize = Files.size(path);
            
            // 创建文件记录并提交
            AssignmentSubmission submission = new AssignmentSubmission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(userId);
            submission.setContent(content);
            submission.setFilePath(filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath);
            submission.setFileSize(fileSize);
            submission.setFileType(fileType);
            submission.setStatus(0); // 未批改
            
            // 保存提交
            submissionService.save(submission);
            return Result.success(submission.getId(), "提交成功");
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 提交作业 (兼容路径)
     */
    @PostMapping("/submit")
    @ApiOperation("提交作业")
    public Result<Long> submitAlternative(
            @ApiParam("作业ID") @RequestParam Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        return submit(assignmentId, content, file, filePath, userId);
    }

    /**
     * 提交作业 (按作业ID)
     */
    @PostMapping("/assignment/{assignmentId}")
    @ApiOperation("提交作业")
    public Result<Long> submitByAssignmentId(
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        return submit(assignmentId, content, file, filePath, userId);
    }

    /**
     * 提交作业 (按作业ID和学生ID)
     */
    @PostMapping("/assignment/{assignmentId}/student/{studentId}")
    @ApiOperation("提交作业")
    public Result<Long> submitByAssignmentAndStudent(
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @ApiParam("学生ID") @PathVariable Long studentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        // 只允许管理员或教师代学生提交
        if (!studentId.equals(userId)) {
            // TODO: 检查权限
        }
        return submit(assignmentId, content, file, filePath, studentId);
    }

    /**
     * 学生提交作业 (按学生ID和作业ID)
     */
    @PostMapping("/student/{studentId}/assignment/{assignmentId}")
    @ApiOperation("学生提交作业")
    public Result<Long> submitByStudentAndAssignment(
            @ApiParam("学生ID") @PathVariable Long studentId,
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        // 只允许学生本人提交
        if (!studentId.equals(userId)) {
            return Result.error("无权代他人提交作业");
        }
        return submit(assignmentId, content, file, filePath, userId);
    }

    /**
     * 通用提交处理器 - 捕获任何POST请求
     */
    @PostMapping("/**")
    @ApiOperation("通用提交处理器")
    public Result<Long> genericSubmitHandler(
            @ApiParam("作业ID") @RequestParam(required = false) Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        if (assignmentId == null) {
            return Result.error("作业ID不能为空");
        }
        return submit(assignmentId, content, file, null, userId);
    }

    /**
     * 批改作业
     */
    @PutMapping("/grade")
    @ApiOperation("批改作业")
    public Result<Boolean> grade(
            @ApiParam("提交ID") @RequestParam Long id, 
            @ApiParam("分数") @RequestParam Integer score, 
            @ApiParam("评语") @RequestParam(required = false) String comment) {
        boolean result = submissionService.gradeSubmission(id, score, comment);
        return Result.success(result, "批改成功");
    }
    
    /**
     * 批改作业（POST方法）
     */
    @PostMapping("/grade")
    @ApiOperation("批改作业")
    public Result<Boolean> gradeWithPost(
            @ApiParam("提交ID") @RequestParam Long id, 
            @ApiParam("分数") @RequestParam Integer score, 
            @ApiParam("评语") @RequestParam(required = false) String comment) {
        return grade(id, score, comment);
    }
    
    /**
     * 上传作业文件
     */
    @PostMapping("/upload")
    @ApiOperation("上传作业文件")
    public Result<Map<String, String>> uploadFile(
            @ApiParam("文件") @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "assignments", userId.toString());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回文件路径
            Map<String, String> result = new HashMap<>();
            result.put("filePath", "/uploads/assignments/" + userId + "/" + filename);
            result.put("fileName", originalFilename);
            
            return Result.success(result, "上传成功");
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载作业文件
     */
    @GetMapping("/download/{id}")
    @ApiOperation("下载作业文件")
    public ResponseEntity<Resource> downloadFile(@ApiParam("提交ID") @PathVariable Long id, HttpServletRequest request) {
        try {
            // 获取提交信息
            AssignmentSubmission submission = submissionService.getById(id);
            if (submission == null || submission.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件路径
            String filePath = submission.getFilePath();
            if (filePath.startsWith("/uploads/")) {
                filePath = filePath.substring("/uploads/".length());
            }
            Path path = Paths.get(uploadDir).resolve(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            // 检查文件是否存在
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件名
            String filename = path.getFileName().toString();
            if (submission.getFilePath().contains("/")) {
                filename = submission.getFilePath().substring(submission.getFilePath().lastIndexOf("/") + 1);
            }
            
            // 对文件名进行URL编码，处理中文和特殊字符
            String encodedFileName = URLEncoder.encode(filename, "UTF-8")
                .replaceAll("\\+", "%20"); // 替换URL编码中的+为%20，以确保空格正确显示
            
            // 根据不同浏览器设置不同的Content-Disposition头
            String userAgent = request.getHeader("User-Agent");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            if (userAgent != null && userAgent.contains("MSIE")) {
                // IE浏览器
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFileName);
            } else if (userAgent != null && userAgent.contains("Firefox")) {
                // Firefox浏览器
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            } else {
                // Chrome、Safari等其他浏览器 - 同时提供两种格式以增加兼容性
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            }
            
            // 增加强制下载头，避免浏览器直接打开一些类型的文件
            headers.set("Content-Transfer-Encoding", "binary");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新提交 (按ID)
     */
    @PutMapping("/{id}")
    @ApiOperation("更新提交")
    public Result<Boolean> update(
            @ApiParam("提交ID") @PathVariable Long id,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        if (id == null) {
            return Result.error("提交ID不能为空");
        }
        
        // 获取提交记录
        AssignmentSubmission submission = submissionService.getById(id);
        if (submission == null) {
            return Result.error("提交记录不存在");
        }
        
        // 检查是否是本人的提交
        if (!submission.getStudentId().equals(userId)) {
            return Result.error("无权修改他人的提交");
        }
        
        // 更新内容
        if (content != null) {
            submission.setContent(content);
        }
        
        // 更新文件
        if (file != null && !file.isEmpty()) {
            try {
                String filePath1 = submissionService.saveFile(file);
                submission.setFilePath(filePath1);
                submission.setFileSize(file.getSize());
                submission.setFileType(file.getContentType());
            } catch (IOException e) {
                return Result.error("文件上传失败：" + e.getMessage());
            }
        } else if (filePath != null && !filePath.isEmpty()) {
            try {
                // 处理从上传接口获取的文件路径
                Path path = null;
                if (filePath.startsWith("/uploads/")) {
                    String relativePath = filePath.substring("/uploads/".length());
                    path = Paths.get(uploadDir).resolve(relativePath);
                } else {
                    path = Paths.get(uploadDir).resolve(filePath);
                }
                
                if (Files.exists(path)) {
                    String fileType = Files.probeContentType(path);
                    long fileSize = Files.size(path);
                    
                    submission.setFilePath(filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length()) : filePath);
                    submission.setFileSize(fileSize);
                    submission.setFileType(fileType);
                } else {
                    return Result.error("文件不存在");
                }
            } catch (Exception e) {
                return Result.error("文件处理失败：" + e.getMessage());
            }
        }
        
        // 更新状态
        submission.setStatus(0); // 重置为未批改
        
        // 保存更新
        boolean success = submissionService.updateById(submission);
        return success ? Result.success(true, "更新成功") : Result.error("更新失败");
    }

    /**
     * 提交作业 (按课程和作业ID)
     */
    @PostMapping("/course/{courseId}/assignment/{assignmentId}")
    @ApiOperation("提交课程作业")
    public Result<Long> submitCourseAssignment(
            @ApiParam("课程ID") @PathVariable Long courseId,
            @ApiParam("作业ID") @PathVariable Long assignmentId,
            @ApiParam("提交内容") @RequestParam(required = false) String content,
            @ApiParam("文件") @RequestParam(required = false) MultipartFile file,
            @ApiParam("文件路径") @RequestParam(required = false) String filePath,
            @RequestAttribute("userId") Long userId) {
        return submit(assignmentId, content, file, filePath, userId);
    }
}