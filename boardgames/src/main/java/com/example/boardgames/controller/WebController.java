package com.example.boardgames.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
public class WebController {

    @GetMapping("/")
    public String homePage() {
        return "<h1>Главная страница</h1>";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "<h1>Личный кабинет</h1>";
    }

    @GetMapping("/catalog")
    public String catalogPage() {
        return "<h1>Каталог игр</h1>";
    }
}

