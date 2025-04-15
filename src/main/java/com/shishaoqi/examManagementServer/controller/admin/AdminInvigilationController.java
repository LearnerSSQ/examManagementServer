package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.dto.InvigilationRecordDTO;
import com.shishaoqi.examManagementServer.dto.InvigilatorAssignmentDTO;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecord;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecordType;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import com.shishaoqi.examManagementServer.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Tag(name = "监考管理", description = "处理监考分配和记录相关的接口")
@Controller
@RequestMapping("/api/admin/invigilation")
public class AdminInvigilationController {

    private static final Logger log = LoggerFactory.getLogger(AdminInvigilationController.class);

    @Autowired
    private InvigilationRecordService invigilationRecordService;

    @Autowired
    private InvigilatorAssignmentService invigilatorAssignmentService;

    @Operation(summary = "创建监考分配", description = "创建新的监考任务分配")
    @ApiResponse(responseCode = "200", description = "成功创建监考分配", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    @PostMapping("/assignments")
    public Result<InvigilatorAssignment> createAssignment(@RequestBody InvigilatorAssignment assignment) {
        invigilatorAssignmentService.save(assignment);
        return Result.success(assignment);
    }

    @Operation(summary = "获取监考分配列表", description = "获取指定时间范围内的监考分配列表")
    @ApiResponse(responseCode = "200", description = "成功获取监考分配列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilatorAssignment.class))))
    @GetMapping("/manage")
    public String showManagementPage() {
        return "admin/invigilation";
    }

    @GetMapping("/assignments")
    public Result<List<InvigilatorAssignment>> listAssignments(
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(invigilatorAssignmentService.getByTimeRange(startTime, endTime));
        }
        return Result.success(invigilatorAssignmentService.list());
    }

    @Operation(summary = "获取教师监考分配", description = "获取指定教师在给定时间范围内的监考分配")
    @ApiResponse(responseCode = "200", description = "成功获取教师的监考分配", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilatorAssignment.class))))
    @GetMapping("/assignments/teacher/{teacherId}")
    public Result<List<InvigilatorAssignment>> getTeacherAssignments(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(
                    invigilatorAssignmentService.getTeacherAssignmentsByTimeRange(teacherId, startTime, endTime));
        }
        return Result.success(invigilatorAssignmentService.getTeacherAssignments(teacherId));
    }

    @Autowired
    private TeacherService teacherService;

    @Operation(summary = "获取所有监考安排", description = "管理员获取所有监考安排信息")
    @GetMapping("/assignments/all")
    @PreAuthorize("hasRole('EXAM_ADMIN')")
    @ResponseBody
    public Result<List<InvigilatorAssignmentDTO>> getAllAssignments(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<InvigilatorAssignment> assignments;
            if (startTime != null && endTime != null) {
                assignments = invigilatorAssignmentService.getByTimeRange(startTime, endTime);
            } else {
                assignments = invigilatorAssignmentService.list();
            }
            if (assignments == null) {
                return Result.error(500, "获取监考安排失败：数据为空");
            }

            // 转换为DTO并添加教师姓名
            List<InvigilatorAssignmentDTO> dtoList = new ArrayList<>();
            for (InvigilatorAssignment assignment : assignments) {
                String teacherName = "未分配";
                if (assignment.getTeacherId() != null) {
                    Teacher teacher = teacherService.getById(assignment.getTeacherId());
                    if (teacher != null) {
                        teacherName = teacher.getName();
                    }
                }
                dtoList.add(InvigilatorAssignmentDTO.fromEntity(assignment, teacherName));
            }

            return Result.success(dtoList);
        } catch (Exception e) {
            e.printStackTrace(); // 添加日志输出
            return Result.error(500, "获取监考安排失败：" + e.getMessage());
        }
    }

    @Operation(summary = "创建监考记录", description = "创建新的监考记录")
    @ApiResponse(responseCode = "200", description = "成功创建监考记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilationRecord.class)))
    @PostMapping("/records")
    public Result<InvigilationRecord> createRecord(@RequestBody InvigilationRecord record) {
        invigilationRecordService.save(record);
        return Result.success(record);
    }

    @Operation(summary = "获取监考记录列表", description = "获取指定时间范围内的监考记录列表")
    @ApiResponse(responseCode = "200", description = "成功获取监考记录列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilationRecordDTO.class))))
    @GetMapping("/records")
    @ResponseBody
    public Result<List<InvigilationRecordDTO>> listRecords(
            @Parameter(description = "搜索关键词") @RequestParam(required = false, name = "search", defaultValue = "") String search,
            @Parameter(description = "记录类型") @RequestParam(required = false, name = "type", defaultValue = "all") String type,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        log.info("获取监考记录列表，搜索关键词：{}，记录类型：{}", search, type);
        List<InvigilationRecord> records;

        if (startTime != null && endTime != null) {
            records = invigilationRecordService.getByTimeRange(startTime, endTime);
        } else {
            records = invigilationRecordService.list();
        }

        // 根据类型筛选
        if (type != null && !"all".equals(type)) {
            try {
                int typeIndex = Integer.parseInt(type);
                if (typeIndex >= 0 && typeIndex < InvigilationRecordType.values().length) {
                    InvigilationRecordType recordType = InvigilationRecordType.values()[typeIndex];
                    records = records.stream()
                            .filter(record -> record.getType() == recordType)
                            .collect(Collectors.toList());
                }
            } catch (NumberFormatException e) {
                // 忽略非数字类型参数
            }
        }

        // 根据关键词搜索
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.toLowerCase();
            records = records.stream()
                    .filter(record -> (record.getContent() != null
                            && record.getContent().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }

        // 转换为DTO并添加教师姓名和课程名称
        List<InvigilationRecordDTO> dtoList = new ArrayList<>();
        for (InvigilationRecord record : records) {
            String teacherName = "未知";
            String courseName = "未知监考";

            // 获取教师姓名
            if (record.getCreatorId() != null) {
                Teacher teacher = teacherService.getById(record.getCreatorId());
                if (teacher != null) {
                    teacherName = teacher.getName();
                }
            }

            // 获取课程名称
            if (record.getAssignmentId() != null) {
                InvigilatorAssignment assignment = invigilatorAssignmentService.getById(record.getAssignmentId());
                if (assignment != null) {
                    courseName = assignment.getCourseName();
                }
            }

            dtoList.add(InvigilationRecordDTO.fromEntity(record, teacherName, courseName));
        }

        return Result.success(dtoList);
    }

    @Operation(summary = "获取教师监考记录", description = "获取指定教师在给定时间范围内的监考记录")
    @ApiResponse(responseCode = "200", description = "成功获取教师的监考记录", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilationRecord.class))))
    @GetMapping("/records/teacher/{teacherId}")
    public Result<List<InvigilationRecord>> getTeacherRecords(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result
                    .success(invigilationRecordService.getTeacherRecordsByTimeRange(teacherId, startTime, endTime));
        }
        return Result.success(invigilationRecordService.getTeacherRecords(teacherId));
    }

    @Operation(summary = "获取监考统计信息", description = "获取指定时间范围内的监考统计信息")
    @ApiResponse(responseCode = "200", description = "成功获取监考统计信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, description = "包含监考统计信息的Map")))
    @GetMapping("/statistics")
    @ResponseBody
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "统计类型") @RequestParam(required = false) String type,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            if (startTime == null) {
                startTime = LocalDateTime.now().minusYears(1);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> statistics;
            if ("teacher".equals(type)) {
                statistics = invigilationRecordService.getTeacherInvigilationStatistics(startTime, endTime);
            } else {
                statistics = invigilationRecordService.getInvigilationStatistics(startTime, endTime);
            }
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取监考统计信息失败", e);
            return Result.error(500, "获取监考统计信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取监考管理页面", description = "获取监考管理主页面")
    @GetMapping
    public String getInvigilationDashboard() {
        return "admin/invigilation";
    }

    @Operation(summary = "获取监考管理数据", description = "获取监考管理主页面数据")
    @GetMapping("/dashboard-data")
    @ResponseBody
    public Result<Map<String, Object>> getInvigilationDashboardData() {
        return Result.success(invigilationRecordService.getDashboardData());
    }
}