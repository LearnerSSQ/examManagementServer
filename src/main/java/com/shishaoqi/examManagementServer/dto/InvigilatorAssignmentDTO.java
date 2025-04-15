package com.shishaoqi.examManagementServer.dto;

import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentRole;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监考安排数据传输对象
 * 
 * 该类用于在API响应中包含监考安排信息和关联的教师信息
 * 
 * @author shishaoqi
 * @since 2024-03-11
 */
@Data
public class InvigilatorAssignmentDTO {

    /**
     * 监考安排ID
     */
    private Long assignmentId;

    /**
     * 教师ID
     */
    private Integer teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 考试开始时间
     */
    private LocalDateTime examStart;

    /**
     * 考试结束时间
     */
    private LocalDateTime examEnd;

    /**
     * 考试地点
     */
    private String location;

    /**
     * 监考角色
     */
    private InvigilatorAssignmentRole role;

    /**
     * 监考任务状态
     */
    private InvigilatorAssignmentStatus status;

    /**
     * 分配时间
     */
    private LocalDateTime assignTime;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 从InvigilatorAssignment实体转换为DTO
     * 
     * @param assignment  监考安排实体
     * @param teacherName 教师姓名
     * @return InvigilatorAssignmentDTO对象
     */
    public static InvigilatorAssignmentDTO fromEntity(InvigilatorAssignment assignment, String teacherName) {
        if (assignment == null) {
            return null;
        }

        InvigilatorAssignmentDTO dto = new InvigilatorAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setTeacherId(assignment.getTeacherId());
        dto.setTeacherName(teacherName);
        dto.setCourseName(assignment.getCourseName());
        dto.setExamStart(assignment.getExamStart());
        dto.setExamEnd(assignment.getExamEnd());
        dto.setLocation(assignment.getLocation());
        dto.setRole(assignment.getRole());
        dto.setStatus(assignment.getStatus());
        dto.setAssignTime(assignment.getAssignTime());
        dto.setConfirmTime(assignment.getConfirmTime());

        return dto;
    }
}