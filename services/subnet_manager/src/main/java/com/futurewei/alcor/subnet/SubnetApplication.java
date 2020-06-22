package com.futurewei.alcor.subnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@ComponentScan(value="com.futurewei.alcor.web.json")
@ComponentScan(value="com.futurewei.alcor.subnet")
public class SubnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubnetApplication.class, args);
    }

}
