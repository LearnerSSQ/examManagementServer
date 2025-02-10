package com.shishaoqi.examManagementServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.shishaoqi.examManagementServer.repository")
@RestController
public class ExamManagementServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamManagementServerApplication.class, args);
	}
}
