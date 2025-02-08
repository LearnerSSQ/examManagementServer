package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Teacher;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public interface TeacherService extends IService<Teacher> {

    /**
     * 添加教师
     */
    void addTeacher(Teacher teacher);

    /**
     * 更新教师信息
     */
    boolean updateTeacher(Teacher teacher);

    /**
     * 批量更新教师信息
     */
    boolean batchUpdateTeachers(List<Teacher> teachers);

    /**
     * 获取教师详细信息
     */
    Teacher getTeacherById(Integer teacherId);

    /**
     * 根据部门获取教师列表
     */
    List<Teacher> getTeachersByDepartment(String department);

    /**
     * 根据邮箱查询教师
     */
    Teacher getTeacherByEmail(String email);

    /**
     * 根据手机号查询教师
     */
    Teacher getTeacherByPhone(String phone);

    /**
     * 更新教师状态
     * 
     * @param teacherId 教师ID
     * @param status    状态值
     * @return 是否更新成功
     */
    boolean updateStatus(Integer teacherId, Integer status);

    /**
     * 更新教师最后登录时间
     * 
     * @return
     */
    boolean updateLastLogin(Integer teacherId);

    /**
     * 更新教师职称
     */
    boolean updateTitle(Integer teacherId, String title);

    /**
     * 获取可用于监考的教师列表
     */
    List<Map<String, Object>> getAvailableInvigilators(Double minScore, Integer maxAssignments);

    /**
     * 获取教师培训完成状态
     */
    List<Map<String, Object>> getTeacherTrainingStatus(String department);

    /**
     * 获取教师监考经验统计
     */
    List<Map<String, Object>> getTeacherExperienceStats(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 检查教师时间冲突
     */
    List<Map<String, Object>> checkTimeConflicts(Integer teacherId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取部门监考任务统计
     */
    List<Map<String, Object>> getDepartmentWorkloadStats();

    /**
     * 获取教师工作量统计
     */
    Map<String, Object> getTeacherWorkloadStats(Integer teacherId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 检查教师是否具备监考资格
     */
    boolean checkInvigilationQualification(Integer teacherId);

    /**
     * 获取教师综合评价报告
     */
    Map<String, Object> generateTeacherReport(Integer teacherId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取部门教师排名
     */
    List<Map<String, Object>> getDepartmentTeacherRanking(String department);

    /**
     * 获取教师培训完成率
     */
    Map<String, Object> getTeacherTrainingCompletion(Integer teacherId);

    /**
     * 获取合格监考教师列表
     * 
     * @param minScore 最低评分要求
     * @return 符合条件的教师列表
     */
    List<Teacher> getQualifiedInvigilators(double minScore);

    /**
     * 获取教师监考统计信息
     * 
     * @param teacherId 教师ID
     * @return 包含监考次数、平均评分等信息
     */
    Map<String, Object> getInvigilationStatistics(Integer teacherId);

    /**
     * 批量更新教师监考资格
     * 
     * @param teacherIds 教师ID列表
     * @param qualified  是否具有资格
     * @return 更新结果
     */
    boolean batchUpdateQualification(List<Integer> teacherIds, boolean qualified);

    /**
     * 获取教师考务工作量统计
     * 
     * @param teacherId 教师ID
     * @param year      年份
     * @return 包含各月份工作量的统计信息
     */
    Map<String, Integer> getWorkloadStatistics(Integer teacherId, int year);

    /**
     * 获取最佳监考教师排名
     * 
     * @param limit 限制数量
     * @return 排名列表
     */
    List<Map<String, Object>> getTopInvigilators(int limit);

    /**
     * 检查教师是否有时间冲突
     * 
     * @param teacherId 教师ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 是否存在冲突
     */
    boolean checkTimeConflict(Integer teacherId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取教师监考历史评价（默认最近一年）
     * 
     * @param teacherId 教师ID
     * @return 历史评价列表
     */
    List<Map<String, Object>> getInvigilationHistory(Integer teacherId);

    /**
     * 获取教师监考历史评价（带时间范围和分页）
     * 
     * @param teacherId 教师ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageSize  每页数量
     * @param pageNum   页码
     * @return 历史评价列表
     */
    List<Map<String, Object>> getInvigilationHistory(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime, int pageSize, int pageNum);

    /**
     * 更新教师专业信息
     * 
     * @param teacherId   教师ID
     * @param specialties 专业领域列表
     * @return 更新结果
     */
    boolean updateSpecialties(Integer teacherId, List<String> specialties);

    /**
     * 教师登录
     * 
     * @param email    邮箱
     * @param password 密码
     * @return 登录成功返回教师信息，失败返回 null
     */
    Teacher login(String email, String password);

    /**
     * 验证密码
     * 
     * @param password       明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean verifyPassword(String password, String hashedPassword);
}