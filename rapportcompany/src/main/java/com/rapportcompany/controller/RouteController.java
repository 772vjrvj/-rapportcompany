package com.rapportcompany.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RouteController {
    @GetMapping({"/"})
    public String index() {
        return "index";
    }

    @GetMapping({"/about"})
    public String about() {
        return "pages/about";
    }

    @GetMapping({"/service"})
    public String service() {
        return "pages/service";
    }

    @GetMapping({"/program"})
    public String program() {
        return "pages/program";
    }

    @GetMapping({"/event"})
    public String event() {
        return "pages/event";
    }

    @GetMapping({"/blog"})
    public String blog() {
        return "pages/blog";
    }

    @GetMapping({"/team"})
    public String team() {
        return "pages/team";
    }

    @GetMapping({"/testimonial"})
    public String testimonial() {
        return "pages/testimonial";
    }

    @GetMapping({"/contact"})
    public String contact() {
        return "pages/contact";
    }

}
