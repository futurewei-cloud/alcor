package com.futurewei.alcor.gatewaymanager;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DbBaseConfiguration.class})
public class GatewayManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayManagerApplication.class, args);
    }
}
