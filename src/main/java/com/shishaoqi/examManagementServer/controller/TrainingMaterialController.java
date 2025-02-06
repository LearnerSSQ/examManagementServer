package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "培训材料管理")
@RestController
@RequestMapping("/api/training-materials")
public class TrainingMaterialController {

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    @Operation(summary = "获取所有已发布的培训材料")
    @GetMapping("/published")
    public Result<List<TrainingMaterial>> getPublishedMaterials() {
        List<TrainingMaterial> materials = trainingMaterialService.getPublishedMaterials();
        return Result.success(materials);
    }

    @Operation(summary = "根据类型获取培训材料")
    @GetMapping("/type/{type}")
    public Result<List<TrainingMaterial>> getMaterialsByType(@PathVariable Integer type) {
        List<TrainingMaterial> materials = trainingMaterialService.getMaterialsByType(type);
        return Result.success(materials);
    }

    @Operation(summary = "更新培训材料状态")
    @PutMapping("/{materialId}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long materialId,
            @RequestParam Integer status) {
        boolean success = trainingMaterialService.updateStatus(materialId, status);
        return Result.success(success);
    }

    @Operation(summary = "更新培训材料通过分数")
    @PutMapping("/{materialId}/pass-score")
    public Result<Boolean> updatePassScore(
            @PathVariable Long materialId,
            @RequestParam Integer passScore) {
        boolean success = trainingMaterialService.updatePassScore(materialId, passScore);
        return Result.success(success);
    }

    @Operation(summary = "创建培训材料")
    @PostMapping
    public Result<TrainingMaterial> createMaterial(@RequestBody TrainingMaterial material) {
        boolean success = trainingMaterialService.save(material);
        return success ? Result.success(material) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    @Operation(summary = "更新培训材料")
    @PutMapping("/{materialId}")
    public Result<TrainingMaterial> updateMaterial(
            @PathVariable Long materialId,
            @RequestBody TrainingMaterial material) {
        material.setMaterialId(materialId);
        boolean success = trainingMaterialService.updateById(material);
        return success ? Result.success(material) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    @Operation(summary = "删除培训材料")
    @DeleteMapping("/{materialId}")
    public Result<Boolean> deleteMaterial(@PathVariable Long materialId) {
        boolean success = trainingMaterialService.removeById(materialId);
        return Result.success(success);
    }
}