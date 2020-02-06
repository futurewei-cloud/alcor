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

package com.futurewei.alcor.controller.app;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import com.futurewei.alcor.controller.cache.config.RedisConfiguration;
import com.futurewei.alcor.controller.logging.Log;
import com.futurewei.alcor.controller.logging.LogFactory;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfig;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfigLoader;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.NodeManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@SpringBootApplication(scanBasePackages = "com.futurewei.alcor.controller")
@Import({RedisConfiguration.class})
public class AlcorControllerApp {
    public static void main(String[] args) {
        Log alcorLog = LogFactory.getLog();
        alcorLog.log(Level.INFO, "Hello Alcor Controller!");
        //Class<?>[] sources = {Alcor.class, RedisConfiguration.class};
        SpringApplication.run(AlcorControllerApp.class, args);
        alcorLog.log(Level.INFO,"Bye from Alcor Controller!\n\n");

        alcorLog.log(Level.INFO,"Loading node from config/machine.json");
        List<HostInfo> hostNodeList = new DataCenterConfigLoader().loadAndGetHostNodeList("/app/config/machine.json");
        if (OneBoxConfig.IS_K8S) {
            if(alcorLog != null)
                alcorLog.log(Level.INFO,"Loading Node Manager");
            DataCenterConfig.nodeManager = new NodeManager(hostNodeList);
        } else if (OneBoxConfig.IS_Onebox) {
            OneBoxConfig.epHosts = OneBoxUtil.LoadNodes(hostNodeList);
        }

        alcorLog.log(Level.INFO,"Load " + hostNodeList.size() + " nodes from machine.json");
        OneBoxConfig.APP_START_TS = System.nanoTime();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            Log alcorLog = LogFactory.getLog();
            alcorLog.log(Level.INFO, "Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                alcorLog.log(Level.INFO,beanName);
            }
        };
    }
}
