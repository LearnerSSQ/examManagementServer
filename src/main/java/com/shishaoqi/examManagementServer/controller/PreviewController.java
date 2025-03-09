package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;

@Controller
@RequestMapping("/preview")
public class PreviewController {

    private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);
    private static final String UPLOAD_DIR = "src/main/resources/static";

    @GetMapping("/pdf")
    public ResponseEntity<Resource> previewPdf(@RequestParam("file") String filePath) {
        try {
            // 检查文件路径是否已经包含UPLOAD_DIR前缀
            if (filePath.startsWith(UPLOAD_DIR)) {
                // 如果已经包含完整路径，直接使用
                Path path = Paths.get(filePath);
                File file = path.toFile();

                if (!file.exists()) {
                    throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
                }

                Resource resource = new FileSystemResource(file);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                // 如果不包含完整路径，则添加UPLOAD_DIR前缀
                // 如果filePath以/开头，则去掉开头的/，因为UPLOAD_DIR已经包含了路径分隔符
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                Path path = Paths.get(UPLOAD_DIR, filePath);
                File file = path.toFile();

                if (!file.exists()) {
                    throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
                }

                Resource resource = new FileSystemResource(file);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            }
        } catch (Exception e) {
            logger.error("PDF预览失败", e);
            String errorJson = "{\"code\": 6001, \"message\": \"文件预览失败\", \"data\": null}";
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(errorJson.getBytes()));
        }
    }

    @GetMapping("/video")
    public ResponseEntity<Resource> previewVideo(@RequestParam("file") String filePath) {
        try {
            // 检查文件路径是否已经包含UPLOAD_DIR前缀
            if (filePath.startsWith(UPLOAD_DIR)) {
                // 如果已经包含完整路径，直接使用
                Path path = Paths.get(filePath);
                File file = path.toFile();

                if (!file.exists()) {
                    throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
                }

                Resource resource = new FileSystemResource(file);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("video/mp4"));
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                // 如果不包含完整路径，则添加UPLOAD_DIR前缀
                // 如果filePath以/开头，则去掉开头的/，因为UPLOAD_DIR已经包含了路径分隔符
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                Path path = Paths.get(UPLOAD_DIR, filePath);
                File file = path.toFile();

                if (!file.exists()) {
                    throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
                }

                Resource resource = new FileSystemResource(file);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("video/mp4"));
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            }
        } catch (Exception e) {
            logger.error("视频预览失败", e);
            String errorJson = "{\"code\": 6001, \"message\": \"文件预览失败\", \"data\": null}";
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(errorJson.getBytes()));
        }
    }
}