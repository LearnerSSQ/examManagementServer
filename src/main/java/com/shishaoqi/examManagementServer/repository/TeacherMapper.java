package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
    
    /**
     * 根据部门查询教师列表
     */
    @Select("SELECT * FROM teacher WHERE department = #{department}")
    List<Teacher> selectByDepartment(String department);

    /**
     * 根据邮箱查询教师
     */
    @Select("SELECT * FROM teacher WHERE email = #{email}")
    Teacher selectByEmail(String email);

    /**
     * 根据手机号查询教师
     */
    @Select("SELECT * FROM teacher WHERE phone = #{phone}")
    Teacher selectByPhone(String phone);
}