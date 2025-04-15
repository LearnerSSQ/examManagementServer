package com.shishaoqi.examManagementServer.dto;

import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecord;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecordStatus;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecordType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监考记录数据传输对象
 * 
 * 扩展了InvigilationRecord实体，添加了教师姓名等前端展示需要的字段
 * 
 * @author shishaoqi
 * @since 2024-03-12
 */
@Data
public class InvigilationRecordDTO {

    /**
     * 监考记录ID
     */
    private Long recordId;

    /**
     * 监考安排ID
     */
    private Long assignmentId;

    /**
     * 记录类型
     */
    private InvigilationRecordType type;

    /**
     * 记录状态
     */
    private InvigilationRecordStatus status;

    /**
     * 记录内容
     */
    private String content;

    /**
     * 创建者ID（教师ID）
     */
    private Integer creatorId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 监考课程名称
     */
    private String courseName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核者ID（教师ID）
     */
    private Integer reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 附件路径（JSON数组）
     */
    private String attachments;
    
    /**
     * 将实体对象转换为DTO对象
     * 
     * @param record 监考记录实体
     * @param teacherName 教师姓名
     * @param courseName 课程名称
     * @return DTO对象
     */
    public static InvigilationRecordDTO fromEntity(InvigilationRecord record, String teacherName, String courseName) {
        if (record == null) {
            return null;
        }
        
        InvigilationRecordDTO dto = new InvigilationRecordDTO();
        dto.setRecordId(record.getRecordId());
        dto.setAssignmentId(record.getAssignmentId());
        dto.setType(record.getType());
        dto.setStatus(record.getStatus());
        dto.setContent(record.getContent());
        dto.setCreatorId(record.getCreatorId());
        dto.setTeacherName(teacherName);
        dto.setCourseName(courseName);
        dto.setCreateTime(record.getCreateTime());
        dto.setSubmitTime(record.getSubmitTime());
        dto.setReviewerId(record.getReviewerId());
        dto.setReviewTime(record.getReviewTime());
        dto.setReviewComment(record.getReviewComment());
        dto.setAttachments(record.getAttachments());
        
        return dto;
    }
}