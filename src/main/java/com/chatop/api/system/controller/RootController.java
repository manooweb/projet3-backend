package com.chatop.api.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Serves the web application home page.
 */
@Hidden
@Controller
public class RootController {

    /**
     * Displays the home page.
     *
     * @return the Thymeleaf home template name
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }
}
