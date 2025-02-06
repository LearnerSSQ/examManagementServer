package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 根据部门查询教师列表
     */
    @Select("SELECT * FROM teacher WHERE department = #{department}")
    List<Teacher> selectByDepartment(@Param("department") String department);

    /**
     * 根据邮箱查询教师
     */
    @Select("SELECT * FROM teacher WHERE email = #{email}")
    Teacher selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询教师
     */
    @Select("SELECT * FROM teacher WHERE phone = #{phone}")
    Teacher selectByPhone(@Param("phone") String phone);

    @Update("UPDATE teacher SET status = #{status} WHERE teacher_id = #{teacherId}")
    int updateStatus(@Param("teacherId") Integer teacherId, @Param("status") Integer status);

    @Update("UPDATE teacher SET last_login = #{lastLogin} WHERE teacher_id = #{teacherId}")
    int updateLastLogin(@Param("teacherId") Integer teacherId, @Param("lastLogin") LocalDateTime lastLogin);
}