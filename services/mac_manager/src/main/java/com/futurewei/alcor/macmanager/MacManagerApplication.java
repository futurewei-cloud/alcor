package com.futurewei.alcor.macmanager;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DbBaseConfiguration.class)
public class MacManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacManagerApplication.class, args);
    }

}
