package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TrainingMaterialServiceTest {

    @Autowired
    private TrainingMaterialService materialService;

    @MockBean
    private TrainingMaterialMapper materialMapper;

    private TrainingMaterial testMaterial;

    @BeforeEach
    void setUp() {
        testMaterial = new TrainingMaterial();
        testMaterial.setMaterialId(1L);
        testMaterial.setTitle("监考规范培训");
        testMaterial.setDescription("介绍监考工作的基本规范和要求");
        testMaterial.setContent("监考工作是保障考试公平公正的重要环节...");
        testMaterial.setType(1);
        testMaterial.setRequiredMinutes(60);
        testMaterial.setStatus(1);
        testMaterial.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getPublishedMaterials() {
        List<TrainingMaterial> materials = Arrays.asList(testMaterial);
        when(materialMapper.selectList(any())).thenReturn(materials);

        List<TrainingMaterial> result = materialService.getPublishedMaterials();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("监考规范培训", result.get(0).getTitle());
        assertEquals(1, result.get(0).getStatus());
    }

    @Test
    void getMaterialsByType() {
        List<TrainingMaterial> materials = Arrays.asList(testMaterial);
        when(materialMapper.selectList(any())).thenReturn(materials);

        List<TrainingMaterial> result = materialService.getMaterialsByType(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getType());
        assertEquals(60, result.get(0).getRequiredMinutes());
    }
}