package com.futurewei.alcor.netwconfigmanager;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import com.futurewei.alcor.web.json.JsonHandlerConfiguration;
import com.futurewei.alcor.web.rbac.aspect.RbacConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({JsonHandlerConfiguration.class, DbBaseConfiguration.class, RbacConfiguration.class})
public class NetworkConfigManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkConfigManagerApplication.class, args);
    }

}
