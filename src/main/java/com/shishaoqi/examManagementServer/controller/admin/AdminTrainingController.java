package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialType;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.util.FileStorageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "培训管理", description = "处理培训材料和记录相关的接口")
@Controller
@RequestMapping("/admin/training")
public class AdminTrainingController {

    private static final Logger log = LoggerFactory.getLogger(AdminTrainingController.class);

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    @Autowired
    private TrainingRecordService trainingRecordService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @GetMapping
    public String trainingPage(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(required = false) TrainingMaterialStatus status,
            @RequestParam(required = false) TrainingMaterialType type,
            Model model) {
        // 创建查询条件
        Page<TrainingMaterial> page = new Page<>(currentPage, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TrainingMaterial> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        // 添加筛选条件
        if (status != null) {
            queryWrapper.eq(TrainingMaterial::getStatus, status);
        }
        if (type != null) {
            queryWrapper.eq(TrainingMaterial::getType, type);
        }
        // 获取培训材料分页数据
        Page<TrainingMaterial> materialPage = trainingMaterialService.page(page, queryWrapper);
        log.info("分页查询参数 - 当前页: {}, 每页大小: {}, 状态: {}, 类型: {}",
                currentPage, pageSize, status, type);
        // 添加模型数据
        model.addAttribute("materials", materialPage.getRecords());
        model.addAttribute("currentPage", currentPage > 0 ? currentPage : 1);
        model.addAttribute("pageSize", pageSize > 0 ? pageSize : 9);
        model.addAttribute("totalPages", materialPage.getPages());
        model.addAttribute("totalItems", materialPage.getTotal());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        return "admin/training";
    }

    @Operation(summary = "上传培训材料", description = "创建新的培训材料")
    @ApiResponse(responseCode = "200", description = "成功上传培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    @PostMapping("/materials")
    public Result<TrainingMaterial> uploadMaterial(@RequestBody TrainingMaterial material) {
        // 设置初始状态为待审核
        material.setStatus(TrainingMaterialStatus.PENDING); // 修复：使用枚举值
        trainingMaterialService.save(material);
        return Result.success(material);
    }

    @Operation(summary = "上传培训材料文件", description = "上传培训材料文件")
    @ApiResponse(responseCode = "200", description = "成功上传文件", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    @PostMapping("/uploadMaterial")
    @ResponseBody
    public Result<Object> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("creatorId") Integer creatorId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") TrainingMaterialType type,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "isRequired", defaultValue = "false") Boolean isRequired,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "passScore", required = false) Integer passScore) {
        if (!fileStorageUtil.isValidFileType(file)) {
            return Result.error(400, "不支持的文件类型");
        }

        // 创建TrainingMaterial对象并设置所有必要字段
        TrainingMaterial material = new TrainingMaterial();
        material.setCreatorId(creatorId);
        material.setTitle(title);
        material.setDescription(description);
        material.setType(type);
        material.setStatus(TrainingMaterialStatus.PENDING);
        material.setDuration(duration);
        material.setIsRequired(isRequired);
        material.setTags(tags);
        material.setPassScore(passScore);
        material.setCreateTime(LocalDateTime.now());

        // 设置临时content值
        material.setContent("pending_upload");

        trainingMaterialService.addTrainingMaterial(material);
        // 获取刚刚添加的材料，确保使用正确的ID类型
        TrainingMaterial savedMaterial = trainingMaterialService.getMaterialById(material.getMaterialId());

        if (savedMaterial == null) {
            log.error("无法获取刚刚保存的培训材料，ID: {}", material.getMaterialId());
            return Result.error(500, "保存培训材料失败");
        }

        try {
            // 存储文件并获取文件路径
            String filePath = fileStorageUtil.storeFile(file, savedMaterial);
            savedMaterial.setContent(filePath);

            // 更新培训材料的内容路径
            trainingMaterialService.updateTrainingMaterial(savedMaterial);
            log.info("培训材料文件上传成功，路径: {}", filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("material", savedMaterial);
            response.put("filePath", filePath);
            return Result.success(response);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error(500, "文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误: {}", e.getMessage(), e);
            return Result.error(500, "文件上传过程中发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "审核培训材料", description = "更新培训材料审核状态")
    @ApiResponse(responseCode = "200", description = "成功更新状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    @PutMapping("/materials/{id}/status")
    public Result<TrainingMaterial> updateMaterialStatus(
            @Parameter(description = "材料ID", required = true) @PathVariable Integer id,
            @Parameter(description = "新状态", required = true) @RequestParam TrainingMaterialStatus newStatus,
            @Parameter(description = "审核意见") @RequestParam(required = false) String reviewComment) {
        try {
            TrainingMaterial material = trainingMaterialService.getById(id);
            if (material == null) {
                log.error("培训材料不存在，ID: {}", id);
                return Result.error(404, "培训材料不存在");
            }

            material.setStatus(newStatus);

            boolean updated = trainingMaterialService.updateById(material);
            if (!updated) {
                log.error("更新培训材料状态失败，ID: {}, 新状态: {}", id, newStatus);
                return Result.error(500, "更新状态失败");
            }

            log.info("培训材料状态更新成功，ID: {}, 新状态: {}", id, newStatus);
            return Result.success(material);
        } catch (Exception e) {
            log.error("更新培训材料状态时发生错误，ID: {}, 新状态: {}, 错误: {}", id, newStatus, e.getMessage(), e);
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }

    @GetMapping("/materials")
    public String listMaterials(Model model) {
        model.addAttribute("materials", trainingMaterialService.list());
        return "admin/training :: #materialsContent";
    }
    
    /**
     * 获取培训材料列表（JSON格式）
     * 用于前端AJAX请求获取培训材料数据
     */
    @GetMapping("/materials/list")
    @ResponseBody
    public Result<List<TrainingMaterial>> getMaterialsList(
            @RequestParam(required = false) TrainingMaterialStatus status) {
        try {
            LambdaQueryWrapper<TrainingMaterial> queryWrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                queryWrapper.eq(TrainingMaterial::getStatus, status);
            }
            List<TrainingMaterial> materials = trainingMaterialService.list(queryWrapper);
            log.info("获取培训材料列表成功，状态过滤: {}, 数量: {}", status, materials.size());
            return Result.success(materials);
        } catch (Exception e) {
            log.error("获取培训材料列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取培训材料列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public String listRecords(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Model model) {
        List<TrainingRecord> records;
        if (startTime != null && endTime != null) {
            records = trainingRecordService.getByTimeRange(startTime, endTime);
        } else {
            records = trainingRecordService.list();
        }
        model.addAttribute("records", records);
        return "admin/training :: #recordsContent";
    }

    @GetMapping("/materials/{id}")
    @ResponseBody
    public Result<Map<String, Object>> getMaterial(@PathVariable Integer id) {
        TrainingMaterial material = trainingMaterialService.getById(id);
        if (material == null) {
            return Result.error(404, "培训材料不存在");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("material", material);

        // 根据文件类型生成预览URL
        if (material.getContent() != null) {
            try {
                Path path = Paths.get(material.getContent());
                String contentType = Files.probeContentType(path);
                if (contentType != null) {
                    if (contentType.startsWith("video/")) {
                        response.put("previewType", "video");
                    } else if (contentType.equals("application/pdf")) {
                        response.put("previewType", "pdf");
                    } else {
                        response.put("previewType", "download");
                    }
                    response.put("previewUrl", "/admin/training/materials/" + id + "/preview");
                }
            } catch (IOException e) {
                log.error("获取文件类型失败: {}", e.getMessage());
                response.put("previewType", "download");
            }
        }

        return Result.success(response);
    }

    @Operation(summary = "创建培训记录", description = "创建新的培训学习记录")
    @ApiResponse(responseCode = "200", description = "成功创建培训记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
    @PostMapping("/records")
    public Result<TrainingRecord> createRecord(@RequestBody TrainingRecord record) {
        trainingRecordService.save(record);
        return Result.success(record);
    }

    @GetMapping("/records/teacher/{teacherId}")
    public String getTeacherRecords(
            @PathVariable Integer teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Model model) {
        List<TrainingRecord> records;
        if (startTime != null && endTime != null) {
            records = trainingRecordService.getTeacherRecordsByTimeRange(teacherId, startTime, endTime);
        } else {
            records = trainingRecordService.getTeacherRecords(teacherId);
        }
        model.addAttribute("teacherRecords", records);
        return "admin/training :: #teacherRecordsContent";
    }

    @GetMapping({ "/statistics", "/stats" })
    public String getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Model model) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Map<String, Object> statistics = trainingRecordService.getTrainingStatistics(startTime, endTime);
        model.addAttribute("statistics", statistics);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);

        return "admin/training :: #statisticsContent";
    }

    @Operation(summary = "下载培训材料", description = "下载指定ID的培训材料文件")
    @ApiResponse(responseCode = "200", description = "成功下载文件", content = @Content(mediaType = "application/octet-stream"))
    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Integer id) throws IOException {
        TrainingMaterial material = trainingMaterialService.getById(id);
        if (material == null || material.getContent() == null) {
            throw new FileNotFoundException("文件不存在");
        }

        Path path = Paths.get(material.getContent());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("无法读取文件");
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "搜索培训记录", description = "根据关键词分页搜索培训记录")
    @GetMapping("/search")
    public Result<Page<TrainingRecord>> searchTrainings(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        log.info("搜索参数 - 关键词: {}, 页码: {}, 每页大小: {}", keyword, page, size);
        Page<TrainingRecord> resultPage = new Page<>(page - 1, size);
        return Result.success(trainingRecordService.search(keyword, resultPage));
    }

    @Operation(summary = "删除培训材料", description = "删除指定ID的培训材料")
    @ApiResponse(responseCode = "200", description = "成功删除培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
    @DeleteMapping("/materials/{id}")
    @ResponseBody
    public Result<Void> deleteMaterial(@PathVariable Integer id) {
        TrainingMaterial material = trainingMaterialService.getById(id);

        // 如果存在文件，删除文件
        if (material.getContent() != null) {
            try {
                Path path = Paths.get(material.getContent());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("删除培训材料文件失败: {}", e.getMessage());
            }
        }

        // 删除数据库记录
        trainingMaterialService.removeById(id);
        return Result.success(null);
    }
    
    /**
     * 培训分配页面
     */
    @GetMapping("/assign")
    public String assignTrainingPage(Model model) {
        log.info("访问培训分配页面");
        return "admin/assign-training";
    }

    @Operation(summary = "获取可分配培训的教师列表", description = "获取可以分配培训的教师列表")
    @ApiResponse(responseCode = "200", description = "成功获取教师列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
    @GetMapping("/assign/teachers")
    @ResponseBody
    public Result<List<Teacher>> getAssignableTeachers(
            @Parameter(description = "部门名称") @RequestParam(required = false) String department,
            @Parameter(description = "教师状态") @RequestParam(required = false) TeacherStatus status) {
        try {
            log.info("获取可分配培训的教师列表，部门: {}, 状态: {}", department, status);

            // 创建查询条件
            LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();

            // 添加筛选条件
            if (department != null && !department.isEmpty()) {
                queryWrapper.eq(Teacher::getDepartment, department);
            }

            if (status != null) {
                queryWrapper.eq(Teacher::getStatus, status);
            } else {
                // 默认只查询激活状态的教师
                queryWrapper.eq(Teacher::getStatus, TeacherStatus.ACTIVE);
            }

            List<Teacher> teachers = teacherService.list(queryWrapper);
            log.info("获取到{}名可分配培训的教师", teachers.size());

            return Result.success(teachers);
        } catch (Exception e) {
            log.error("获取可分配培训的教师列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取教师列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分配培训给教师", description = "将培训材料分配给指定教师")
    @ApiResponse(responseCode = "200", description = "成功分配培训", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
    @PostMapping("/assign")
    @ResponseBody
    public Result<Map<String, Object>> assignTraining(
            @Parameter(description = "培训材料ID", required = true) @RequestParam Long materialId,
            @Parameter(description = "教师ID列表", required = true) @RequestParam List<Integer> teacherIds) {
        try {
            log.info("开始分配培训，材料ID: {}, 教师数量: {}", materialId, teacherIds.size());

            // 验证培训材料是否存在
            TrainingMaterial material = trainingMaterialService.getById(materialId);
            if (material == null) {
                log.error("培训材料不存在，ID: {}", materialId);
                return Result.error(404, "培训材料不存在");
            }

            // 验证培训材料状态是否为已发布
            if (material.getStatus() != TrainingMaterialStatus.PUBLISHED) {
                log.error("培训材料未发布，无法分配，ID: {}, 状态: {}", materialId, material.getStatus());
                return Result.error(400, "培训材料未发布，无法分配");
            }

            // 创建培训记录
            List<TrainingRecord> createdRecords = new ArrayList<>();
            List<Integer> failedTeacherIds = new ArrayList<>();

            for (Integer teacherId : teacherIds) {
                try {
                    // 检查教师是否存在
                    Teacher teacher = teacherService.getById(teacherId);
                    if (teacher == null) {
                        log.warn("教师不存在，ID: {}", teacherId);
                        failedTeacherIds.add(teacherId);
                        continue;
                    }

                    // 检查是否已经分配过该培训
                    LambdaQueryWrapper<TrainingRecord> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(TrainingRecord::getTeacherId, teacherId)
                            .eq(TrainingRecord::getMaterialId, materialId);

                    if (trainingRecordService.count(queryWrapper) > 0) {
                        log.warn("教师已分配过该培训，教师ID: {}, 培训ID: {}", teacherId, materialId);
                        continue;
                    }

                    // 创建培训记录
                    TrainingRecord record = trainingRecordService.createRecord(teacherId, materialId);
                    createdRecords.add(record);
                    log.info("成功为教师{}分配培训{}", teacherId, materialId);
                } catch (Exception e) {
                    log.error("为教师{}分配培训{}失败: {}", teacherId, materialId, e.getMessage(), e);
                    failedTeacherIds.add(teacherId);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalAssigned", createdRecords.size());
            result.put("totalFailed", failedTeacherIds.size());
            result.put("assignedRecords", createdRecords);
            result.put("failedTeacherIds", failedTeacherIds);

            log.info("培训分配完成，成功: {}, 失败: {}", createdRecords.size(), failedTeacherIds.size());
            return Result.success(result);
        }catch(

    Exception e)
    {
        log.error("分配培训失败: {}", e.getMessage(), e);
        return Result.error(500, "分配培训失败: " + e.getMessage());
    }
    }

    @Operation(summary = "批量取消培训分配", description = "取消指定教师的培训分配")
    @ApiResponse(responseCode = "200", description = "成功取消培训分配", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
    @DeleteMapping("/assign")
    @ResponseBody
    public Result<Map<String, Object>> cancelTrainingAssignment(
            @Parameter(description = "培训记录ID列表", required = true) @RequestParam List<Long> recordIds) {
        try {
            log.info("开始取消培训分配，记录数量: {}", recordIds.size());

            List<Long> successIds = new ArrayList<>();
            List<Long> failedIds = new ArrayList<>();

            for (Long recordId : recordIds) {
                try {
                    // 获取培训记录
                    TrainingRecord record = trainingRecordService.getById(recordId);
                    if (record == null) {
                        log.warn("培训记录不存在，ID: {}", recordId);
                        failedIds.add(recordId);
                        continue;
                    }

                    // 只能取消未开始的培训
                    if (record.getStatus() != TrainingRecordStatus.NOT_STARTED) {
                        log.warn("培训已开始或已完成，无法取消，ID: {}, 状态: {}", recordId, record.getStatus());
                        failedIds.add(recordId);
                        continue;
                    }

                    // 删除培训记录
                    boolean success = trainingRecordService.removeById(recordId);
                    if (success) {
                        successIds.add(recordId);
                        log.info("成功取消培训分配，记录ID: {}", recordId);
                    } else {
                        failedIds.add(recordId);
                        log.error("取消培训分配失败，记录ID: {}", recordId);
                    }
                } catch (Exception e) {
                    log.error("取消培训分配失败，记录ID: {}, 错误: {}", recordId, e.getMessage(), e);
                    failedIds.add(recordId);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalSuccess", successIds.size());
            result.put("totalFailed", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);

            log.info("取消培训分配完成，成功: {}, 失败: {}", successIds.size(), failedIds.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量取消培训分配失败: {}", e.getMessage(), e);
            return Result.error(500, "批量取消培训分配失败: " + e.getMessage());
        }
    }
}