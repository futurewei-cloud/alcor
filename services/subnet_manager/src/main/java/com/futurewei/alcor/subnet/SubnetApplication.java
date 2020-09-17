package com.futurewei.alcor.subnet;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import com.futurewei.alcor.web.json.JsonHandlerConfiguration;
import com.futurewei.alcor.web.rbac.aspect.RbacConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Import({DbBaseConfiguration.class, JsonHandlerConfiguration.class, RbacConfiguration.class})
public class SubnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubnetApplication.class, args);
    }

}
