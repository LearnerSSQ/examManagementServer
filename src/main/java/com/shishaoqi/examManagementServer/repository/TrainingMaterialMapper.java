package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TrainingMaterialMapper extends BaseMapper<TrainingMaterial> {

    /**
     * 更新培训材料状态
     */
    @Update("UPDATE training_material SET status = #{status} WHERE material_id = #{materialId}")
    int updateStatus(@Param("materialId") Long materialId, @Param("status") Integer status);

    /**
     * 更新考试通过分数
     */
    @Update("UPDATE training_material SET pass_score = #{passScore} WHERE material_id = #{materialId}")
    int updatePassScore(@Param("materialId") Long materialId, @Param("passScore") Integer passScore);
}