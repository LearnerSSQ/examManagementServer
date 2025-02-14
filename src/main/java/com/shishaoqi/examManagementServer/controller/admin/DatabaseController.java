package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api/admin/database")
public class DatabaseController {

    private static final Logger log = LoggerFactory.getLogger(DatabaseController.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("classpath:db/generate_database.sql")
    private Resource generateDatabaseSql;

    @Value("classpath:db/initialize_data.sql")
    private Resource initializeDataSql;

    public DatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String databasePage() {
        return "admin/database";
    }

    @PostMapping("/init")
    @ResponseBody
    public Result<Void> initDatabase() {
        try {
            String sql = StreamUtils.copyToString(generateDatabaseSql.getInputStream(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
            log.info("数据库初始化成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            return Result.error(500, "数据库初始化失败：" + e.getMessage());
        }
    }

    @PostMapping("/init-data")
    @ResponseBody
    public Result<Void> initData() {
        try {
            String sql = StreamUtils.copyToString(initializeDataSql.getInputStream(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
            log.info("数据初始化成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("数据初始化失败", e);
            return Result.error(500, "数据初始化失败：" + e.getMessage());
        }
    }

    @PostMapping("/reset-data")
    @ResponseBody
    public Result<Void> resetData() {
        try {
            // 清空所有表数据但保留表结构
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            String[] tables = {
                    "teacher", "exam", "invigilator_assignment", "invigilation_record",
                    "training_material", "training_record", "evaluation"
            };

            for (String table : tables) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            log.info("数据重置成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("数据重置失败", e);
            return Result.error(500, "数据重置失败：" + e.getMessage());
        }
    }
}