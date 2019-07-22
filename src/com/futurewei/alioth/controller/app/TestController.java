package com.futurewei.alioth.controller.app;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class TestController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Alioth!";
    }

}