package com.futurewei.alcor.subnet;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import com.futurewei.alcor.web.json.JsonHandlerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Import({DbBaseConfiguration.class, JsonHandlerConfiguration.class})
public class SubnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubnetApplication.class, args);
    }

}
