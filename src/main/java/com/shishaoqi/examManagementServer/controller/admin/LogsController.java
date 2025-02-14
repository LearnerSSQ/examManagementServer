package com.shishaoqi.examManagementServer.controller.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/api/admin/logs")
public class LogsController {

    private static final Logger log = LoggerFactory.getLogger(LogsController.class);
    private static final String LOG_FILE_PATH = "logs/app.log";
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
    public String getLogContent() throws IOException {
        return formatLogContent(readLogFile());
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
        Path logPath = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(logPath)) {
            return "日志文件不存在";
        }

        // 读取最新的1000行日志
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(logPath)) {
            lines = stream.collect(Collectors.toList());
            int startIndex = Math.max(0, lines.size() - 1000);
            lines = lines.subList(startIndex, lines.size());
        }

        return String.join("\n", lines);
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