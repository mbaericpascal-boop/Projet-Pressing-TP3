package com.tp.pressing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Redirige vers login.html
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Redirige vers index.html (ton accueil)
    }
}