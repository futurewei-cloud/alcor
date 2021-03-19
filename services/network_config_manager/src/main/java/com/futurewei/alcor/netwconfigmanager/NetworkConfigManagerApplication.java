package com.futurewei.alcor.netwconfigmanager;

import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.server.grpc.GoalStateProvisionerServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NetworkConfigManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkConfigManagerApplication.class, args);

        NetworkConfigServer networkConfigServer = new GoalStateProvisionerServer();
        try {
            networkConfigServer.start();
            networkConfigServer.blockUntilShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}