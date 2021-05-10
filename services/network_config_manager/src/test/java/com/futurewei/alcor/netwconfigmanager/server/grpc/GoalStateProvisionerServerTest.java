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
package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.client.gRPC.GoalStateClientImpl;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.util.DemoUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SpringBootTest
@Component
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.server")
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.server.grpc")
@AutoConfigureMockMvc
public class GoalStateProvisionerServerTest {

    private static final Logger logger = LoggerFactory.getLogger();

    @Autowired
    private NetworkConfigServer networkConfigServer;

    private ManagedChannel channel;
    private GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub;
    private GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub;

    public GoalStateProvisionerServerTest() {
        super();
    }

    /**
     * Check host on-demand response
     */
//    @Test
    public void testNetworkConfigServer() throws InterruptedException {
        try {
            pushGoalState();
            String response = getHostResponse();
            assertEquals("success", response);
        } finally {
            shutdown();
        }
    }

    private String pushGoalState() {
        logger.log(Level.INFO, "Will get host on-demand response...");

        Map<String, HostGoalState> hostGoalStates = new HashMap<>();
        DemoUtil.populateHostGoalState(hostGoalStates, DemoUtil.aca_node_one_ip, DemoUtil.aca_node_two_ip);

        logger.log(Level.INFO, "requestGoalStates : send GS to ACA " + DemoUtil.aca_node_one_ip + " | ",
                hostGoalStates.get(DemoUtil.aca_node_one_ip).getGoalState().toString());

        try {
            GoalStateClient grpcGoalStateClient = new GoalStateClientImpl();
            grpcGoalStateClient.sendGoalStates(hostGoalStates);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }

    private String getHostResponse() {
        logger.log(Level.INFO, "Will get host on-demand response...");

        Goalstateprovisioner.HostRequest.ResourceStateRequest stateRequest =
                Goalstateprovisioner.HostRequest.ResourceStateRequest.newBuilder()
                        .setRequestId("XXX")
                        .setTunnelId(21)
                        .setSourceIp("10.0.0.2")
                        .setSourcePort(8080)
                        .setDestinationIp("10.0.0.3")
                        .setDestinationPort(9090)
                        .setEthertype(Common.EtherType.IPV4)
                        .setProtocol(Common.Protocol.ARP)
                        .build();
        Goalstateprovisioner.HostRequest request = Goalstateprovisioner.HostRequest.newBuilder()
                .setFormatVersion(1)
                .addStateRequests(stateRequest).build();
        Goalstateprovisioner.HostRequestReply response;
        try {
            response = blockingStub.requestGoalStates(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            fail();
            return "";
        }

        return response.toString();
    }

    @Before
    public void beforeEachTest() throws InstantiationException, IllegalAccessException, IOException {
//        networkConfigServer = new GoalStateProvisionerServer();
//        networkConfigServer.start();
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9016)
                .usePlaintext()
                .build();
        blockingStub = GoalStateProvisionerGrpc.newBlockingStub(channel);
        asyncStub = GoalStateProvisionerGrpc.newStub(channel);
    }

    @After
    public void afterEachTest() throws InterruptedException {
        channel.shutdownNow();
//        networkConfigServer.stop();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
