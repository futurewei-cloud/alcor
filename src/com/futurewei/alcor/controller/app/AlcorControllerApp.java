package com.futurewei.alcor.controller.app;

import java.util.Arrays;
import java.util.List;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import com.futurewei.alcor.controller.cache.config.*;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.DataCenterConfigLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.futurewei.alcor.controller")
@Import({ RedisConfiguration.class})
public class AlcorControllerApp {
    public static void main(String[] args) {
        System.out.println("Hello Alcor Controller!");
        //Class<?>[] sources = {Alcor.class, RedisConfiguration.class};
        SpringApplication.run(AlcorControllerApp.class, args);
        System.out.println("Bye from Alcor Controller!\n\n");

        System.out.println("Loading node from config/machine.json");
        List<HostInfo> hostNodeList = DataCenterConfigLoader.loadAndGetHostNodeList("/app/config/machine.json"); //".\\config\\machine.json");
        if(OneBoxConfig.IS_Demo){
            OneBoxUtil.AssignNodes(hostNodeList);
        }
        System.out.println("Load " + hostNodeList.size() + " nodes from machine.json");
        OneBoxConfig.APP_START_TS = System.nanoTime();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }
}
