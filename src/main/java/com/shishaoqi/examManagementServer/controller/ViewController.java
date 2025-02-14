package com.shishaoqi.examManagementServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
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
        return "invigilation";
    }

    @GetMapping("/training")
    public String trainingGet() {
        return "training";
    }
}