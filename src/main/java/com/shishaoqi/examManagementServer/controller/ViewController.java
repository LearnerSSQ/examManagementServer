package com.shishaoqi.examManagementServer.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.shishaoqi.examManagementServer.security.TeacherUserDetails;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal TeacherUserDetails userDetails) {
        if (userDetails != null && userDetails.getTeacher() != null) {
            return "redirect:/api/profile";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/teachers")
    public String teachersGet() {
        return "teachers";
    }

    @GetMapping("/invigilation")
    public String invigilationGet() {
        return "admin/invigilation";
    }

    @GetMapping("/training")
    public String trainingGet() {
        return "training";
    }

    @GetMapping("/admin/logs")
    public String logsPage() {
        return "admin/logs";
    }

    @GetMapping("/training/manage")
    public String trainingManage() {
        return "training";
    }

    @GetMapping("/training/my")
    public String myTraining() {
        return "training";
    }

    @GetMapping("/assignments/manage")
    public String assignmentsManage() {
        return "invigilation";
    }

    @GetMapping("/assignments/my")
    public String myAssignments() {
        return "invigilation";
    }

    @GetMapping("/admin/settings")
    public String settingsPage() {
        return "admin/settings";
    }

    @GetMapping("/admin/database")
    public String databasePage() {
        return "admin/database";
    }

    @GetMapping("/api/assignments/manage")
    public String assignmentsManageApi() {
        return "invigilation";
    }

    @GetMapping("/api/training/manage")
    public String trainingManageApi() {
        return "training";
    }

    @GetMapping("/api/teachers")
    public String teachersApi() {
        return "teachers";
    }
}