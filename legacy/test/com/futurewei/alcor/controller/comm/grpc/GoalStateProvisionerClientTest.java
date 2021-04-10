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


package com.futurewei.alcor.controller.comm.grpc;

import com.futurewei.alcor.controller.schema.Common;
import com.futurewei.alcor.controller.service.Goalstateprovisioner;
import com.futurewei.alcor.controller.schema.Goalstate.*;
import com.futurewei.alcor.controller.schema.Vpc;
import com.futurewei.alcor.controller.service.GoalStateProvisionerGrpc;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class GoalStateProvisionerClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final GoalStateProvisionerGrpc.GoalStateProvisionerImplBase serviceImpl =
            mock(GoalStateProvisionerGrpc.GoalStateProvisionerImplBase.class,
                    delegatesTo(new GoalStateProvisionerGrpc.GoalStateProvisionerImplBase() {
                    }));
    private GoalStateProvisionerClient client;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a client using the in-process channel;
        client = new GoalStateProvisionerClient(channel);
    }

    /**
     * To test the client, call from the client against the fake server, and verify behaviors or state
     * changes from the server side.
     */
    @Test
    public void greet_messageDeliveredToServer() {
        final Vpc.VpcState vpc_state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final Vpc.VpcState vpc_state2 = GoalStateUtil.CreateGSVpcState(Common.OperationType.UPDATE,
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "MiniVpc",
                "192.168.1.0/29");

        GoalState goalstate = GoalState.newBuilder()
                .addVpcStates(vpc_state)
                .addVpcStates(vpc_state2)
                .build();

        ArgumentCaptor<GoalState> requestCaptor = ArgumentCaptor.forClass(GoalState.class);
        client.PushNetworkResourceStates(goalstate);

        verify(serviceImpl)
                .pushNetworkResourceStates(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Goalstateprovisioner.GoalStateOperationReply>>any());
        assertEquals(goalstate.getVpcStatesCount(), requestCaptor.getValue().getVpcStatesCount());
        assertEquals(goalstate.getSubnetStatesCount(), requestCaptor.getValue().getSubnetStatesCount());
        assertEquals(goalstate.getPortStatesCount(), requestCaptor.getValue().getPortStatesCount());
        assertEquals(goalstate.getSecurityGroupStatesCount(), requestCaptor.getValue().getSecurityGroupStatesCount());
    }
}