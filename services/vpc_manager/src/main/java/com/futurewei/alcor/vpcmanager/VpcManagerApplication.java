/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.vpcmanager;

import com.futurewei.alcor.common.db.DbBaseConfiguration;
import com.futurewei.alcor.web.json.JsonHandlerConfiguration;
import com.futurewei.alcor.web.rbac.aspect.RbacConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication()
@EnableAutoConfiguration
@ComponentScan({"com.futurewei.alcor.vpcmanager"})
@Import({DbBaseConfiguration.class, JsonHandlerConfiguration.class, RbacConfiguration.class})
public class VpcManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VpcManagerApplication.class, args);
    }

}
