package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/api/admin/settings")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    private static final ConcurrentHashMap<String, Object> systemSettings = new ConcurrentHashMap<>();

    @Value("${server.port}")
    private String serverPort;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private String tokenExpiration;

    @Value("classpath:application.yml")
    private Resource applicationYml;

    @GetMapping
    public String settingsPage(Model model) {
        try {
            // 读取YAML配置文件内容
            String yamlContent = StreamUtils.copyToString(applicationYml.getInputStream(), StandardCharsets.UTF_8);

            // 添加配置到模型
            model.addAttribute("serverPort", serverPort);
            model.addAttribute("dbUrl", dbUrl);
            model.addAttribute("dbUsername", dbUsername);
            model.addAttribute("dbPassword", dbPassword);
            model.addAttribute("jwtSecret", jwtSecret);
            model.addAttribute("tokenExpiration", tokenExpiration);
            model.addAttribute("yamlConfig", yamlContent);

            return "admin/settings";
        } catch (Exception e) {
            log.error("加载配置失败", e);
            return "error";
        }
    }

    @PostMapping("/server")
    @ResponseBody
    public Result<Void> saveServerConfig(@RequestBody Map<String, String> config) {
        try {
            // 更新application.yml中的服务器配置
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(StreamUtils.copyToString(
                    applicationYml.getInputStream(), StandardCharsets.UTF_8));

            // 更新配置
            updateNestedValue(yamlMap, "server.port", config.get("serverPort"));
            updateNestedValue(yamlMap, "spring.datasource.url", config.get("dbUrl"));
            updateNestedValue(yamlMap, "spring.datasource.username", config.get("dbUsername"));
            updateNestedValue(yamlMap, "spring.datasource.password", config.get("dbPassword"));

            // 保存更新后的配置
            try (FileWriter writer = new FileWriter(applicationYml.getFile())) {
                yaml.dump(yamlMap, writer);
            }

            log.info("服务器配置更新成功");
            return Result.success("服务器配置更新成功");
        } catch (Exception e) {
            log.error("服务器配置更新失败", e);
            return Result.error(500, "服务器配置更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/security")
    @ResponseBody
    public Result<Void> saveSecurityConfig(@RequestBody Map<String, String> config) {
        try {
            // 更新application.yml中的安全配置
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(StreamUtils.copyToString(
                    applicationYml.getInputStream(), StandardCharsets.UTF_8));

            // 更新配置
            updateNestedValue(yamlMap, "jwt.secret", config.get("jwtSecret"));
            updateNestedValue(yamlMap, "jwt.expiration", config.get("tokenExpiration"));

            // 保存更新后的配置
            try (FileWriter writer = new FileWriter(applicationYml.getFile())) {
                yaml.dump(yamlMap, writer);
            }

            log.info("安全配置更新成功");
            return Result.success("安全配置更新成功");
        } catch (Exception e) {
            log.error("安全配置更新失败", e);
            return Result.error(500, "安全配置更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/yaml")
    @ResponseBody
    public Result<Void> saveYamlConfig(@RequestBody Map<String, String> config) {
        try {
            // 验证YAML格式
            Yaml yaml = new Yaml();
            yaml.load(config.get("yamlContent")); // 如果格式不正确会抛出异常

            // 保存YAML内容
            try (FileWriter writer = new FileWriter(applicationYml.getFile())) {
                writer.write(config.get("yamlContent"));
            }

            log.info("YAML配置更新成功");
            return Result.success("YAML配置更新成功");
        } catch (Exception e) {
            log.error("YAML配置更新失败", e);
            return Result.error(500, "YAML配置更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/system")
    @ResponseBody
    public Result<Map<String, Object>> getSystemSettings() {
        try {
            return Result.success(new HashMap<>(systemSettings));
        } catch (Exception e) {
            log.error("获取系统设置失败", e);
            return Result.error(500, "获取系统设置失败：" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateNestedValue(Map<String, Object> map, String path, String value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = map;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new HashMap<String, Object>();
                current.put(parts[i], next);
            }
            current = (Map<String, Object>) next;
        }

        current.put(parts[parts.length - 1], value);
    }
}