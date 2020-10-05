package com.futurewei.alcor.securitygroup;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import com.futurewei.alcor.web.json.JsonHandlerConfiguration;
import com.futurewei.alcor.web.rbac.aspect.RbacConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DbBaseConfiguration.class, JsonHandlerConfiguration.class, RbacConfiguration.class})
public class SecurityGroupManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityGroupManagerApplication.class, args);
    }

}
