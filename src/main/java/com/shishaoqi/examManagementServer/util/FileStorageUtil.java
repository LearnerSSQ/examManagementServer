package com.shishaoqi.examManagementServer.util;

import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件存储工具类
 * 用于处理培训材料的文件存储和路径生成
 */
@Component
public class FileStorageUtil {

    private static final String BASE_DIR = "src/main/resources/static/training-materials";

    /**
     * 生成文件存储路径
     * 
     * @param material 培训材料实体
     * @return 文件存储路径
     */
    public String generateFilePath(TrainingMaterial material) {
        LocalDate now = LocalDate.now();
        if (material.getMaterialId() == null) {
            throw new IllegalArgumentException("材料ID不能为空");
        }
        return Paths.get(
                BASE_DIR,
                String.valueOf(now.getYear()),
                String.format("%02d", now.getMonthValue()),
                String.format("%02d", now.getDayOfMonth()),
                material.getCreatorId().toString(),
                material.getMaterialId().toString(),
                "files").toString();
    }

    /**
     * 存储文件
     * 
     * @param file     上传的文件
     * @param material 培训材料实体
     * @return 存储的文件路径
     * @throws IOException 文件存储异常
     */
    public String storeFile(MultipartFile file, TrainingMaterial material) throws IOException {
        String filePath = generateFilePath(material);
        Path storagePath = Paths.get(filePath);

        // 创建目录
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // 存储文件
        Path targetLocation = storagePath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation);

        // 将Windows风格的反斜杠替换为正斜杠
        return targetLocation.toString().replace("\\", "/");
    }

    /**
     * 获取文件访问URL
     * 
     * @param filePath 文件存储路径
     * @return 文件访问URL
     */
    public String getFileAccessUrl(String filePath) {
        return filePath.replace("src/main/resources/static", "");
    }

    /**
     * 获取文件预览URL
     * 
     * @param filePath 文件存储路径
     * @return 文件预览URL
     */
    public String getPreviewUrl(String filePath) {
        // 将文件系统路径转换为URL路径
        String urlPath = filePath.replace("src/main/resources/static", "");
        // 根据文件类型返回不同的预览URL
        String fileExtension = urlPath.substring(urlPath.lastIndexOf(".")).toLowerCase();
        switch (fileExtension) {
            case ".pdf":
                return "/preview/pdf?file=" + urlPath;
            case ".mp4":
            case ".webm":
            case ".ogg":
                return "/preview/video?file=" + urlPath;
            default:
                return urlPath;
        }
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @throws IOException 文件删除异常
     */
    public void deleteFile(String filePath) throws IOException {
        // 将URL路径转换为文件系统路径
        String systemPath = "src/main/resources" + filePath;
        Path path = Paths.get(systemPath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    /**
     * 验证文件类型
     * 
     * @param file 上传的文件
     * @return 是否为有效的文件类型
     */
    public boolean isValidFileType(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // 根据不同的培训材料类型验证文件类型
        return contentType.startsWith("application/pdf") ||
                contentType.startsWith("application/msword") ||
                contentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.startsWith("video/") ||
                contentType.equals("application/json");
    }
}