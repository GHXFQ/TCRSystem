package com.tcr.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcr.system.entity.Material;
import com.tcr.system.entity.MaterialReview;
import com.tcr.system.entity.TempMaterial;
import com.tcr.system.exception.BusinessException;
import com.tcr.system.mapper.MaterialMapper;
import com.tcr.system.mapper.MaterialReviewMapper;
import com.tcr.system.mapper.TempMaterialMapper;
import com.tcr.system.mapper.CourseMapper;
import com.tcr.system.mapper.MaterialTypeMapper;
import com.tcr.system.mapper.UserMapper;
import com.tcr.system.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.URLEncoder;

/**
 * 材料服务实现类
 */
@Service
@RequiredArgsConstructor
@Component
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final MaterialReviewMapper materialReviewMapper;
    private final TempMaterialMapper tempMaterialMapper;
    private final CourseMapper courseMapper;
    private final MaterialTypeMapper materialTypeMapper;
    private final UserMapper userMapper;

    @PostConstruct
    public void init() {
        // 创建上传目录
        createDirectory(uploadDir);
    }

    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                log.error("创建目录失败: {}", path);
                throw new BusinessException("创建文件上传目录失败");
            }
        }
    }

    @Override
    public Page<Material> page(Page<Material> page, Long courseId, Integer type) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了课程ID，则按课程ID查询
        if (courseId != null) {
            wrapper.eq(Material::getCourseId, courseId);
        }
        
        // 如果指定了材料类型，则按类型查询
        if (type != null) {
            wrapper.eq(Material::getType, type);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Material::getCreateTime);
        
        // 执行查询
        Page<Material> result = page(page, wrapper);
        
        // 填充上传者信息和课程信息
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Material material : result.getRecords()) {
                // 获取上传者信息
                if (material.getUploadUserId() != null) {
                    try {
                        com.tcr.system.entity.User user = userMapper.selectById(material.getUploadUserId());
                        if (user != null) {
                            // 直接设置上传者名称
                            material.setUploadUserName(user.getRealName());
                        }
                    } catch (Exception e) {
                        log.error("获取上传者信息失败，用户ID: {}", material.getUploadUserId(), e);
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    public Page<Map<String, Object>> getTeacherMaterials(Page<Map<String, Object>> page, Long teacherId, 
                                                     Long courseId, Long materialTypeId, 
                                                     String academicYear, Integer semester, Integer type) {
        // 参数检验
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        try {
            // 调用Mapper查询
            Page<Map<String, Object>> result = baseMapper.getTeacherMaterials(page, teacherId, courseId, 
                                                                       materialTypeId, academicYear, semester, type);
            
            // 如果查询失败或返回空，使用临时解决方案：手动查询
            if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
                log.info("使用备用方案查询教师材料列表，教师ID: {}", teacherId);
                result = getTeacherMaterialsAlternative(page, teacherId, courseId, materialTypeId, academicYear, semester, type);
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询教师材料列表失败", e);
            // 异常时使用备用方案
            return getTeacherMaterialsAlternative(page, teacherId, courseId, materialTypeId, academicYear, semester, type);
        }
    }
    
    /**
     * 备用方案：手动查询教师材料列表
     * 当Mapper查询失败时使用
     */
    private Page<Map<String, Object>> getTeacherMaterialsAlternative(Page<Map<String, Object>> page, Long teacherId, 
                                                                Long courseId, Long materialTypeId, 
                                                                String academicYear, Integer semester, Integer type) {
        // 创建查询条件
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Material::getUploadUserId, teacherId);
        
        if (courseId != null) {
            wrapper.eq(Material::getCourseId, courseId);
        }
        
        if (materialTypeId != null) {
            wrapper.eq(Material::getMaterialTypeId, materialTypeId);
        }
        
        if (!StringUtils.isEmpty(academicYear)) {
            wrapper.eq(Material::getAcademicYear, academicYear);
        }
        
        if (semester != null) {
            wrapper.eq(Material::getSemester, semester);
        }
        
        if (type != null) {
            wrapper.eq(Material::getType, type);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Material::getCreateTime);
        
        // 执行查询
        Page<Material> materialPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Material> materialResult = baseMapper.selectPage(materialPage, wrapper);
        
        // 转换结果
        Page<Map<String, Object>> result = new Page<>();
        result.setCurrent(materialResult.getCurrent());
        result.setSize(materialResult.getSize());
        result.setTotal(materialResult.getTotal());
        result.setPages(materialResult.getPages());
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Material material : materialResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", material.getId());
            map.put("name", material.getName());
            map.put("description", material.getDescription());
            map.put("course_id", material.getCourseId());
            map.put("file_path", material.getFilePath());
            map.put("file_size", material.getFileSize());
            map.put("file_type", material.getFileType());
            map.put("upload_user_id", material.getUploadUserId());
            map.put("download_count", material.getDownloadCount());
            map.put("type", material.getType());
            map.put("academic_year", material.getAcademicYear());
            map.put("semester", material.getSemester());
            map.put("material_type_id", material.getMaterialTypeId());
            map.put("create_time", material.getCreateTime());
            map.put("update_time", material.getUpdateTime());
            
            // 查询关联的审核记录
            LambdaQueryWrapper<MaterialReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(MaterialReview::getMaterialId, material.getId());
            reviewWrapper.orderByDesc(MaterialReview::getCreateTime);
            reviewWrapper.last("LIMIT 1");
            MaterialReview review = materialReviewMapper.selectOne(reviewWrapper);
            
            if (review != null) {
                map.put("review_id", review.getId());
                map.put("review_status", review.getStatus());
            }
            
            // 查询课程名称和材料类型名称
            if (material.getCourseId() != null) {
                // 这里可以使用缓存优化，避免每次查询
                try {
                    com.tcr.system.entity.Course course = courseMapper.selectById(material.getCourseId());
                    if (course != null) {
                        map.put("course_name", course.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取课程名称失败", e);
                }
            }
            
            if (material.getMaterialTypeId() != null) {
                try {
                    com.tcr.system.entity.MaterialType materialType = materialTypeMapper.selectById(material.getMaterialTypeId());
                    if (materialType != null) {
                        map.put("material_type_name", materialType.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取材料类型名称失败", e);
                }
            }
            
            records.add(map);
        }
        
        result.setRecords(records);
        return result;
    }

    @Override
    public Material getById(Long id) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("材料ID不能为空");
        }
        
        return super.getById(id);
    }

    @Override
    @Transactional
    public Long uploadMaterial(String name, String description, Long courseId, MultipartFile file,
                    Long uploadUserId, Integer type, String academicYear, Integer semester,
                    Long materialTypeId) {
        return uploadMaterial(name, description, courseId, file, uploadUserId, type, academicYear, semester, materialTypeId, null);
    }
    
    /**
     * 上传教学材料任务相关的材料
     */
    @Transactional
    public Long uploadTaskMaterial(String name, String description, Long courseId, MultipartFile file,
                    Long uploadUserId, Integer type, String academicYear, Integer semester,
                    Long materialTypeId, Long taskId) {
        return uploadMaterial(name, description, courseId, file, uploadUserId, type, academicYear, semester, materialTypeId, taskId);
    }
    
    /**
     * 通用上传材料实现
     */
    @Transactional
    private Long uploadMaterial(String name, String description, Long courseId, MultipartFile file,
                    Long uploadUserId, Integer type, String academicYear, Integer semester,
                    Long materialTypeId, Long taskId) {
        try {
            // 添加日志，检查接收到的taskId
            log.info("uploadMaterial方法接收到的taskId: {}", taskId);
            
            // 获取文件信息
            String originalFilename = file.getOriginalFilename();
            String fileType = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            long fileSize = file.getSize();
            
            // 生成新的文件名
            String newFilename = UUID.randomUUID().toString() + "." + fileType;
            
            // 创建目录的子目录（按年月组织）
            String currentDate = new SimpleDateFormat("yyyy/MM").format(new Date());
            String relativePath = currentDate + "/" + newFilename;
            
            // 确保目录存在
            File targetDir = new File(uploadDir + "/" + currentDate);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    throw new BusinessException("创建目录失败");
                }
            }
        
        // 保存文件
            File targetFile = new File(uploadDir + "/" + relativePath);
            file.transferTo(targetFile);
        
        // 创建材料记录
            TempMaterial tempMaterial = new TempMaterial();
            tempMaterial.setName(name);
            tempMaterial.setDescription(description);
            tempMaterial.setCourseId(courseId);
            tempMaterial.setFilePath(relativePath);
            tempMaterial.setFileSize(fileSize);
            tempMaterial.setFileType(fileType);
            tempMaterial.setUploadUserId(uploadUserId);
            tempMaterial.setType(type);
            tempMaterial.setAcademicYear(academicYear);
            tempMaterial.setSemester(semester);
            tempMaterial.setMaterialTypeId(materialTypeId);
            tempMaterial.setTaskId(taskId); // 设置关联的任务ID
            
            log.info("即将插入TempMaterial，taskId: {}", tempMaterial.getTaskId());
            tempMaterialMapper.insert(tempMaterial);
            log.info("插入TempMaterial后，ID: {}, taskId: {}", tempMaterial.getId(), tempMaterial.getTaskId());
            
            return tempMaterial.getId();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws IOException {
        // 校验参数
        if (id == null) {
            throw new BusinessException("材料ID不能为空");
        }
        
        // 查询材料
        Material material = getById(id);
        TempMaterial tempMaterial = null;
        
        if (material == null) {
            // 如果在正式表中找不到，尝试在临时表中查找
            tempMaterial = tempMaterialMapper.selectById(id);
            if (tempMaterial == null) {
            throw new BusinessException("材料不存在");
            }
        }
        
        // 使用Spring的RequestContextHolder获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // 获取用户信息，确保类型安全的转换
        Long userId = (Long) request.getAttribute("userId");
        Object roleObj = request.getAttribute("role");
        Integer role = 3; // 默认为学生角色
        
        if (roleObj != null) {
            if (roleObj instanceof Integer) {
                role = (Integer) roleObj;
            } else if (roleObj instanceof String) {
                try {
                    role = Integer.parseInt((String) roleObj);
                } catch (NumberFormatException e) {
                    log.warn("角色类型转换失败，使用默认角色");
                }
            }
        }
        
        // 获取文件信息
        String filePath;
        String fileName;
        String fileType;
        if (material != null) {
            filePath = material.getFilePath();
            fileName = material.getName();
            fileType = material.getFileType();
        } else {
            filePath = tempMaterial.getFilePath();
            fileName = tempMaterial.getName();
            fileType = tempMaterial.getFileType();
            
            // 检查是否有权限下载临时材料
            if (role == 3) { // 学生角色
                throw new BusinessException("该材料尚未审核通过，暂时无法下载");
            }
        }
        
        // 获取文件 - 使用正确的路径拼接方式
        File file = new File(uploadDir + File.separator + filePath.replace("/", File.separator));
        if (!file.exists()) {
            log.error("文件不存在: {}", file.getAbsolutePath());
            throw new BusinessException("文件不存在");
        }
        
        // 确定文件的实际MIME类型
        String mimeType = determineMimeType(fileType);
        
        // 更新下载次数（仅对正式表中的材料）
        if (material != null) {
        material.setDownloadCount(material.getDownloadCount() + 1);
        updateById(material);
        }
        
        // 设置响应头
        response.setContentType(mimeType);
        
        // 更好地处理文件名，确保扩展名正确
        // 如果用户上传时文件没有扩展名，或者扩展名与实际类型不匹配，则根据MIME类型添加正确的扩展名
        String downloadFileName = fileName;
        if (!downloadFileName.toLowerCase().endsWith("." + fileType.toLowerCase())) {
            downloadFileName = downloadFileName + "." + fileType;
        }
        
        log.info("下载文件 - 原始文件名: {}, 下载文件名: {}, MIME类型: {}", fileName, downloadFileName, mimeType);
        
        // 使用标准方式设置Content-Disposition头
        try {
            // 简单的编码方式，直接使用UTF-8 URL编码
            String encodedFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
            
            // 设置基本的Content-Disposition头
            String dispositionValue = "attachment";
            
            // 添加不带引号的filename参数（针对IE）
            dispositionValue += "; filename=" + encodedFileName;
            
            // 添加RFC 5987编码的filename*参数（现代浏览器）
            dispositionValue += "; filename*=UTF-8''" + encodedFileName;
            
            log.info("设置Content-Disposition头: {}", dispositionValue);
            response.setHeader("Content-Disposition", dispositionValue);
        } catch (Exception e) {
            log.error("设置文件名时出错", e);
            // 如果设置失败，使用简单的方式
            response.setHeader("Content-Disposition", "attachment; filename=download." + fileType);
        }
        
        // 增加强制下载头，避免浏览器直接打开一些类型的文件
        response.setHeader("Content-Transfer-Encoding", "binary");
        
        // 复制文件到响应输出流
        try (FileInputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    private String determineMimeType(String fileType) {
        if (fileType == null) {
            return "application/octet-stream";
        }
        
        switch (fileType.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
            default:
                log.warn("未知文件类型: {}, 默认使用application/octet-stream", fileType);
                // 尝试通过文件扩展名识别一些常见的文件类型
                if (fileType.equals("document") || fileType.equals("msword")) {
                    return "application/msword";
                } else if (fileType.equals("sheet") || fileType.equals("excel")) {
                    return "application/vnd.ms-excel";
                } else if (fileType.equals("presentation") || fileType.equals("powerpoint")) {
                    return "application/vnd.ms-powerpoint";
                }
                return "application/octet-stream";
        }
    }

    /**
     * 检查材料的审核状态
     * @param materialId 材料ID
     * @return 是否已审核通过
     */
    private boolean checkMaterialApprovalStatus(Long materialId) {
        LambdaQueryWrapper<MaterialReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialReview::getMaterialId, materialId)
                   .eq(MaterialReview::getDeleted, 0);
        MaterialReview review = materialReviewMapper.selectOne(queryWrapper);
        return review != null && review.getStatus() == 5; // 5表示审核通过
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long id, Long userId, Integer role) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("材料ID不能为空");
        }
        
        // 查询材料
        Material material = getById(id);
        if (material == null) {
            throw new BusinessException("材料不存在");
        }
        
        // 只有材料的上传者或管理员可以删除材料
        if (!material.getUploadUserId().equals(userId) && role != 2) {
            throw new BusinessException("无权删除该材料");
        }
        
        // 删除文件
        try {
            Files.deleteIfExists(Paths.get(uploadDir, material.getFilePath()));
        } catch (IOException e) {
            throw new BusinessException("删除文件失败");
        }
        
        return removeById(id);
    }
    
    @Override
    public Page<Map<String, Object>> getSharedMaterials(Page<Map<String, Object>> page, Long userId, 
                                                   Long courseId, Long[] courseIds, Long materialTypeId, 
                                                   String academicYear, Integer semester, Integer type) {
        // 参数检验
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        try {
            // 调用Mapper查询
            Page<Map<String, Object>> result = baseMapper.getSharedMaterials(page, userId, courseId, 
                                                                     courseIds, materialTypeId, 
                                                                     academicYear, semester, type);
            
            // 如果查询失败或返回空，使用备用方案
            if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
                log.info("使用备用方案查询课程资源协同列表，用户ID: {}", userId);
                result = getSharedMaterialsAlternative(page, userId, courseId, courseIds, 
                                                    materialTypeId, academicYear, semester, type);
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询课程资源协同列表失败", e);
            // 异常时使用备用方案
            return getSharedMaterialsAlternative(page, userId, courseId, courseIds, 
                                              materialTypeId, academicYear, semester, type);
        }
    }
    
    /**
     * 备用方案：手动查询课程资源协同列表
     * 当Mapper查询失败时使用
     */
    private Page<Map<String, Object>> getSharedMaterialsAlternative(Page<Map<String, Object>> page, Long userId, 
                                                             Long courseId, Long[] courseIds, Long materialTypeId, 
                                                             String academicYear, Integer semester, Integer type) {
        // 创建查询条件
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        
        // 安全检查：只能查看自己课程的材料
        if (courseId != null) {
            wrapper.eq(Material::getCourseId, courseId);
        } else if (courseIds != null && courseIds.length > 0) {
            wrapper.in(Material::getCourseId, courseIds);
        }
        
        if (materialTypeId != null) {
            wrapper.eq(Material::getMaterialTypeId, materialTypeId);
        }
        
        if (!StringUtils.isEmpty(academicYear)) {
            wrapper.eq(Material::getAcademicYear, academicYear);
        }
        
        if (semester != null) {
            wrapper.eq(Material::getSemester, semester);
        }
        
        if (type != null) {
            wrapper.eq(Material::getType, type);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Material::getCreateTime);
        
        // 执行查询
        Page<Material> materialPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Material> materialResult = baseMapper.selectPage(materialPage, wrapper);
        
        // 转换结果
        Page<Map<String, Object>> result = new Page<>();
        result.setCurrent(materialResult.getCurrent());
        result.setSize(materialResult.getSize());
        result.setTotal(materialResult.getTotal());
        result.setPages(materialResult.getPages());
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Material material : materialResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", material.getId());
            map.put("name", material.getName());
            map.put("description", material.getDescription());
            map.put("course_id", material.getCourseId());
            map.put("file_path", material.getFilePath());
            map.put("file_size", material.getFileSize());
            map.put("file_type", material.getFileType());
            map.put("upload_user_id", material.getUploadUserId());
            map.put("download_count", material.getDownloadCount());
            map.put("type", material.getType());
            map.put("academic_year", material.getAcademicYear());
            map.put("semester", material.getSemester());
            map.put("material_type_id", material.getMaterialTypeId());
            map.put("create_time", material.getCreateTime());
            map.put("update_time", material.getUpdateTime());
            
            // 查询课程名称和材料类型名称
            if (material.getCourseId() != null) {
                try {
                    com.tcr.system.entity.Course course = courseMapper.selectById(material.getCourseId());
                    if (course != null) {
                        map.put("course_name", course.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取课程名称失败", e);
                }
            }
            
            if (material.getMaterialTypeId() != null) {
                try {
                    com.tcr.system.entity.MaterialType materialType = materialTypeMapper.selectById(material.getMaterialTypeId());
                    if (materialType != null) {
                        map.put("material_type_name", materialType.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取材料类型名称失败", e);
                }
            }
            
            // 查询上传者姓名
            if (material.getUploadUserId() != null) {
                try {
                    com.tcr.system.entity.User user = userMapper.selectById(material.getUploadUserId());
                    if (user != null) {
                        map.put("upload_user_name", user.getRealName());
                    }
                } catch (Exception e) {
                    log.warn("获取上传者姓名失败", e);
                }
            }
            
            records.add(map);
        }
        
        result.setRecords(records);
        return result;
    }
    
    @Override
    public Object getCourseSharedMaterials(Long courseId, Long materialTypeId, Long excludeUserId) {
        // 参数检验
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }
        if (materialTypeId == null) {
            throw new BusinessException("材料类型ID不能为空");
        }
        
        try {
            // 调用Mapper查询
            List<Map<String, Object>> result = baseMapper.getCourseSharedMaterials(courseId, materialTypeId, excludeUserId);
            
            // 如果查询失败或返回空，使用备用方案
            if (result == null || result.isEmpty()) {
                log.info("使用备用方案查询同课程其他教师材料，课程ID: {}, 材料类型ID: {}", courseId, materialTypeId);
                result = getCourseSharedMaterialsAlternative(courseId, materialTypeId, excludeUserId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询同课程其他教师材料失败", e);
            // 异常时使用备用方案
            return getCourseSharedMaterialsAlternative(courseId, materialTypeId, excludeUserId);
        }
    }
    
    /**
     * 备用方案：手动查询同课程其他教师的教学大纲和授课计划
     * 当Mapper查询失败时使用
     */
    private List<Map<String, Object>> getCourseSharedMaterialsAlternative(Long courseId, Long materialTypeId, Long excludeUserId) {
        // 创建查询条件
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Material::getCourseId, courseId);
        wrapper.eq(Material::getMaterialTypeId, materialTypeId);
        
        if (excludeUserId != null) {
            wrapper.ne(Material::getUploadUserId, excludeUserId);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Material::getCreateTime);
        
        // 执行查询
        List<Material> materials = baseMapper.selectList(wrapper);
        
        // 转换结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Material material : materials) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", material.getId());
            map.put("name", material.getName());
            map.put("description", material.getDescription());
            map.put("course_id", material.getCourseId());
            map.put("file_path", material.getFilePath());
            map.put("file_size", material.getFileSize());
            map.put("file_type", material.getFileType());
            map.put("upload_user_id", material.getUploadUserId());
            map.put("download_count", material.getDownloadCount());
            map.put("type", material.getType());
            map.put("academic_year", material.getAcademicYear());
            map.put("semester", material.getSemester());
            map.put("material_type_id", material.getMaterialTypeId());
            map.put("create_time", material.getCreateTime());
            map.put("update_time", material.getUpdateTime());
            
            // 查询课程名称和材料类型名称
            if (material.getCourseId() != null) {
                try {
                    com.tcr.system.entity.Course course = courseMapper.selectById(material.getCourseId());
                    if (course != null) {
                        map.put("course_name", course.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取课程名称失败", e);
                }
            }
            
            if (material.getMaterialTypeId() != null) {
                try {
                    com.tcr.system.entity.MaterialType materialType = materialTypeMapper.selectById(material.getMaterialTypeId());
                    if (materialType != null) {
                        map.put("material_type_name", materialType.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取材料类型名称失败", e);
                }
            }
            
            // 查询上传者姓名
            if (material.getUploadUserId() != null) {
                try {
                    com.tcr.system.entity.User user = userMapper.selectById(material.getUploadUserId());
                    if (user != null) {
                        map.put("upload_user_name", user.getRealName());
                    }
                } catch (Exception e) {
                    log.warn("获取上传者姓名失败", e);
                }
            }
            
            result.add(map);
        }
        
        return result;
    }
    
    @Override
    public void updateDownloadCount(Long id) {
        // 校验参数
        if (id == null) {
            throw new BusinessException("材料ID不能为空");
        }
        
        // 查询材料
        Material material = getById(id);
        if (material == null) {
            throw new BusinessException("材料不存在");
        }
        
        // 更新下载次数
        material.setDownloadCount(material.getDownloadCount() + 1);
        updateById(material);
    }
} 