package com.futurewei.alcor.portmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class PortManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortManagerApplication.class, args);
    }

}
