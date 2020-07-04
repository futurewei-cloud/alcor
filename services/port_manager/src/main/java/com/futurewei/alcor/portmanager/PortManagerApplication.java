package com.futurewei.alcor.portmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value="com.futurewei.alcor.web.json")
@ComponentScan(value="com.futurewei.alcor.portmanager")
public class PortManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortManagerApplication.class, args);
    }

}
