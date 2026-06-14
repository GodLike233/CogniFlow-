package com.cogniflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() { return "landing"; }

    @GetMapping("/welcome")
    public String welcome() { return "welcome"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/index")
    public String chat() { return "index"; }

    @GetMapping("/planning")
    public String planning() { return "planning"; }

    @GetMapping("/plans")
    public String plans() { return "plans"; }

    @GetMapping("/plan-detail/{id}")
    public String planDetail(@PathVariable String id) { return "plan-detail"; }

    @GetMapping("/courses")
    public String courses() { return "courses"; }

    @GetMapping("/exam")
    public String exam() { return "exam"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }
}
