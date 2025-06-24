package com.scprojectjava2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test-logout")
    public String testLogout() {
        return "test-logout";
    }
    
    @GetMapping("/test-simple")
    public String testSimple() {
        return "test-simple";
    }
}
