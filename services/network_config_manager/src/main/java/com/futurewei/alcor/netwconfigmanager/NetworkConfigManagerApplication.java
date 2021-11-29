/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.netwconfigmanager;

import com.futurewei.alcor.common.tracer.TracerConfiguration;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.server.grpc.GoalStateProvisionerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync
@Import(TracerConfiguration.class)
public class NetworkConfigManagerApplication {

    @Autowired
    private NetworkConfigServer networkConfigServer;

    @PostConstruct
    public void instantiateGrpcServer(){
        /*
         * This code is left in for reference only and will be removed
         * in a future PR very soon.
         * This way of starting the gRPC channel breaks the RESP end point.
         * The new method, staring it in it's thread along with a few
         * other changes, autowiring the grpc client instance,
         * using constructor injections and spring boot ensure that
         * REST and gRPC can co-exist.
         *
        try {
            networkConfigServer.start();
            networkConfigServer.blockUntilShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        Thread server = new Thread(() -> {
            try {
                networkConfigServer.start();
                networkConfigServer.blockUntilShutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        server.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(NetworkConfigManagerApplication.class, args);
    }
}
