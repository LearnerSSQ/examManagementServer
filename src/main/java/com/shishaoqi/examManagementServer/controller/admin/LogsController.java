package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')") // 添加管理员权限控制
public class LogsController {

    private static final Logger log = LoggerFactory.getLogger(LogsController.class);

    // 修改为相对路径
    private static final String LOG_DIR = System.getProperty("user.home") + "/exam_logs/";
    private static final String LOG_FILE_PATH = LOG_DIR + "app.log";

    static {
        new File(LOG_DIR).mkdirs();
    }
    private static final Pattern LOG_PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (ERROR|WARN|INFO|DEBUG)");

    @GetMapping
    public String logsPage(Model model) {
        try {
            String logContent = readLogFile();
            model.addAttribute("logContent", formatLogContent(logContent));
            return "admin/logs";
        } catch (IOException e) {
            log.error("读取日志文件失败", e);
            model.addAttribute("error", "读取日志文件失败");
            return "error";
        }
    }

    @GetMapping("/content")
    @ResponseBody
    public Result<String> getLogContent() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists() || !logFile.canRead() || logFile.length() == 0) {
                return Result.error(404, "日志文件不存在或无法读取");
            }

            List<String> lines = Files.readAllLines(logFile.toPath());
            Collections.reverse(lines); // 最新的日志在前面

            StringBuilder formattedLogs = new StringBuilder();
            for (String line : lines.subList(0, Math.min(lines.size(), 1000))) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String timestamp = matcher.group(1);
                    String level = matcher.group(2);
                    String message = line.substring(matcher.end()).trim();

                    formattedLogs.append(String.format(
                            "<div class=\"log-line log-level-%s\">" +
                                    "<div class=\"log-timestamp\">%s</div>" +
                                    "<div class=\"log-badge %s\">%s</div>" +
                                    "<div class=\"log-message\">%s</div>" +
                                    "</div>",
                            level.toLowerCase(),
                            timestamp,
                            level.toLowerCase(),
                            level,
                            message));
                } else {
                    formattedLogs.append(String.format(
                            "<div class=\"log-line\">" +
                                    "<div class=\"log-message\">%s</div>" +
                                    "</div>",
                            line));
                }
            }

            return Result.success(formattedLogs.toString());
        } catch (Exception e) {
            log.error("读取日志文件时发生错误", e);
            return Result.error(500, "读取日志文件失败：" + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogs() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            Resource resource = new FileSystemResource(logFile);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.log\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("下载日志文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String readLogFile() throws IOException {
        Path logPath = Paths.get(LOG_FILE_PATH).toAbsolutePath().normalize();
        log.info("尝试读取日志文件: {}", logPath);

        if (!Files.exists(logPath)) {
            log.error("日志文件不存在: {}", logPath);
            return "";
        }

        try (Stream<String> lines = Files.lines(logPath)) {
            List<String> logLines = lines.collect(Collectors.toList());
            Collections.reverse(logLines); // 最新的日志在前面
            return String.join("\n", logLines);
        } catch (IOException e) {
            log.error("读取日志文件失败: {}", logPath, e);
            throw e;
        }
    }

    private String formatLogContent(String content) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            Matcher matcher = LOG_PATTERN.matcher(line);
            if (matcher.find()) {
                String level = matcher.group(2);
                formatted.append("<div class=\"log-line log-").append(level.toLowerCase()).append("\">")
                        .append(line)
                        .append("</div>");
            } else {
                formatted.append("<div class=\"log-line\">")
                        .append(line)
                        .append("</div>");
            }
        }

        return formatted.toString();
    }
}