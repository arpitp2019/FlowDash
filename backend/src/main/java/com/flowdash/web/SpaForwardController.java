package com.flowdash.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/", "/login", "/goals", "/habits", "/vault", "/decision"})
    public String forward() {
        return "forward:/index.html";
    }
}
