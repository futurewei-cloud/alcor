/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.controller.app;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import com.futurewei.alcor.controller.db.redis.RedisConfiguration;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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
        //Class<?>[] sources = {Alcor.class, RedisConfiguration.class};
        SpringApplication.run(AlcorControllerApp.class, args);
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "Hello Alcor Controller!");
        logger.log(Level.INFO, "Bye from Alcor Controller!\n\n");

        logger.log(Level.INFO, "Loading node from config/machine.json");
        List<HostInfo> hostNodeList = new DataCenterConfigLoader().loadAndGetHostNodeList("/app/config/machine.json");
        if (OneBoxConfig.IS_K8S) {
            logger.log(Level.INFO, "Loading Node Manager");
            DataCenterConfig.nodeManager = new NodeManager(hostNodeList);
        } else if (OneBoxConfig.IS_Onebox) {
            OneBoxConfig.epHosts = OneBoxUtil.LoadNodes(hostNodeList);
        }

        logger.log(Level.INFO, "Load " + hostNodeList.size() + " nodes from machine.json");
        OneBoxConfig.APP_START_TS = System.nanoTime();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            Logger logger = LoggerFactory.getLogger();
            logger.log(Level.INFO, "Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                logger.log(Level.INFO, beanName);
            }
        };
    }
}
