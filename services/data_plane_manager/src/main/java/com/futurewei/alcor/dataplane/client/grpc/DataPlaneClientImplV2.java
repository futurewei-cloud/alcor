/*
 *
 * MIT License
 * Copyright(c) 2020 Futurewei Cloud
 *
 *     Permission is hereby granted,
 *     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
 *     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
 *     to whom the Software is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package com.futurewei.alcor.dataplane.client.grpc;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.entity.MulticastGoalStateV2;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.schema.*;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service("grpcDataPlaneClient")
@ConditionalOnProperty(prefix = "protobuf.goal-state-message", name = "version", havingValue = "102")
public class DataPlaneClientImplV2 implements DataPlaneClient<UnicastGoalStateV2, MulticastGoalStateV2> {

    private static DataPlaneClientImplV2 instance = null;

    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImplV2.class);

    private int hostAgentPort;

    private final ExecutorService executor;

    // each host_ip should have this amount of gRPC channels
    private int numberOfGrpcChannelPerHost;

    // when a channel is set up, send this amount of default GoalStates for warmup.
    private int numberOfWarmupsPerChannel;

    private String netwconfigmanagerGrpcServiceUrl;

    // prints out UUID and time, when sending a GoalState to any of the monitorHosts
    private ArrayList<String> monitorHosts;

    private ConcurrentHashMap<String, ArrayList<GrpcChannelStub>> hostIpGrpcChannelStubMap;

    @Override
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates) throws Exception {
        Goalstate.GoalStateV2.Builder goalStateBuilder = Goalstate.GoalStateV2.newBuilder();
        final CountDownLatch finishLatch = new CountDownLatch(1);
        List<String> results = new ArrayList<>();
        for (UnicastGoalStateV2 unicastGoalState : unicastGoalStates) {
            goalStateBuilder = getGoalState(goalStateBuilder, unicastGoalState);
        }
        System.out.println(goalStateBuilder.build());
        doSendGoalState(goalStateBuilder.build(), finishLatch, results);

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            LOG.warn("Send goal states can not finish within 1 minutes");
            return Arrays.asList("Send goal states can not finish within 1 minutes");
        }
        return results;
    }

    public static DataPlaneClientImplV2 getInstance(Config globalConfig, ArrayList<String> monitorHosts) {
        if (instance == null) {
            instance = new DataPlaneClientImplV2(globalConfig, monitorHosts);
        }
        return instance;
    }

    public DataPlaneClientImplV2(Config globalConfig, ArrayList<String> monitorHosts) {
        // each host should have at least 1 gRPC channel
        if(numberOfGrpcChannelPerHost < 1) {
            numberOfGrpcChannelPerHost = 1;
        }

        // allow users to not send warmups, if they wish to.
        if(numberOfWarmupsPerChannel < 0){
            numberOfWarmupsPerChannel = 0;
        }

        if (netwconfigmanagerGrpcServiceUrl == null || netwconfigmanagerGrpcServiceUrl.isEmpty()) {
            netwconfigmanagerGrpcServiceUrl = globalConfig.netwconfigmanagerGrpcServiceUrl;
        }

        this.monitorHosts = monitorHosts;
        LOG.info("Printing out all monitorHosts");
        for(String host : this.monitorHosts){
            LOG.info("Monitoring this host: " + host);
        }
        LOG.info("Done printing out all monitorHosts");
        this.numberOfGrpcChannelPerHost = globalConfig.numberOfGrpcChannelPerHost;
        this.numberOfWarmupsPerChannel = globalConfig.numberOfWarmupsPerChannel;
        this.hostAgentPort = 9016;

        this.executor = new ThreadPoolExecutor(100,
                200,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory("grpc-thread-pool"));
        //TODO: Setup a connection pool. one ACA, one client.
        this.hostIpGrpcChannelStubMap = new ConcurrentHashMap();
        LOG.info("This instance has "+ numberOfGrpcChannelPerHost+" channels, and "+ numberOfWarmupsPerChannel+" warmups");
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates, MulticastGoalStateV2 multicastGoalState) throws Exception {
        if (unicastGoalStates == null) {
            unicastGoalStates = new ArrayList<>();
        }

        if (multicastGoalState != null &&
                multicastGoalState.getHostIps() != null &&
                multicastGoalState.getGoalState() != null) {
            for (String hostIp: multicastGoalState.getHostIps()) {
                UnicastGoalStateV2 unicastGoalState = new UnicastGoalStateV2();
                unicastGoalState.setHostIp(hostIp);
                unicastGoalState.setGoalState(multicastGoalState.getGoalState());

                unicastGoalStates.add(unicastGoalState);
            }
        }

        if (unicastGoalStates.size() > 0) {
            return sendGoalStates(unicastGoalStates);
        }

        return null;
    }

    private GrpcChannelStub createGrpcChannelStub(String hostIp) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostIp, this.hostAgentPort)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .keepAliveTime(Long.MAX_VALUE, TimeUnit.SECONDS)
                .build();
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = GoalStateProvisionerGrpc.newStub(channel);

        return new GrpcChannelStub(channel, asyncStub);
    }

    private GrpcChannelStub getOrCreateGrpcChannel(String hostIp) {
        if (!this.hostIpGrpcChannelStubMap.containsKey(hostIp)) {
            this.hostIpGrpcChannelStubMap.put(hostIp, createGrpcChannelStubArrayList(hostIp));
            LOG.info("[getOrCreateGrpcChannel] Created a channel and stub to host IP: " + hostIp);
        }
        int usingChannelWithThisIndex = ThreadLocalRandom.current().nextInt(0, numberOfGrpcChannelPerHost);
        ManagedChannel chan = this.hostIpGrpcChannelStubMap.get(hostIp).get(usingChannelWithThisIndex).channel;
        //checks the channel status, reconnects if the channel is IDLE

        ConnectivityState channelState = chan.getState(true);
        if (channelState != ConnectivityState.READY && channelState != ConnectivityState.CONNECTING && channelState != ConnectivityState.IDLE) {
            GrpcChannelStub newChannelStub = createGrpcChannelStub(hostIp);
            this.hostIpGrpcChannelStubMap.get(hostIp).set(usingChannelWithThisIndex, newChannelStub);
            LOG.info("[getOrCreateGrpcChannel] Replaced a channel and stub to host IP: " + hostIp);
        }
        LOG.info("[getOrCreateGrpcChannel] Using channel and stub index " + usingChannelWithThisIndex + " to host IP: " + hostIp);
        return this.hostIpGrpcChannelStubMap.get(hostIp).get(usingChannelWithThisIndex);
    }

    private ArrayList<GrpcChannelStub> createGrpcChannelStubArrayList(String hostIp) {
        long start = System.currentTimeMillis();
        ArrayList<GrpcChannelStub> arr = new ArrayList<>();
        for (int i = 0; i < numberOfGrpcChannelPerHost; i++) {
            GrpcChannelStub channelStub = createGrpcChannelStub(hostIp);
            // Using Linkerd load balance
            //warmUpChannelStub(channelStub, hostIp);
            arr.add(channelStub);
        }
        long end = System.currentTimeMillis();
        LOG.info("[createGrpcChannelStubArrayList] Created " + numberOfGrpcChannelPerHost + " gRPC channel stubs for host " + hostIp + ", elapsed Time in milli seconds: " + (end - start));
        return arr;
    }

    // try to warmup a gRPC channel and its stub, by sending an empty GoalState`.
    void warmUpChannelStub(GrpcChannelStub channelStub, String hostIp) {
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = channelStub.stub;

        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply reply) {
                LOG.info("Receive warmup response from ACA@" + hostIp + " | " + reply.toString());
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Receive warmup error from ACA@" + hostIp + " |  " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                LOG.info("Complete receiving warmup message from ACA@" + hostIp);
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        try {
            Goalstate.GoalStateV2 goalState = Goalstate.GoalStateV2.getDefaultInstance();
            LOG.info("Sending GS to Host " + hostIp + " as follows | " + goalState.toString());
            for (int i = 0; i < numberOfWarmupsPerChannel; i++) {
                requestObserver.onNext(goalState);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            LOG.warn("[doSendGoalState] Sending GS, but error happened | " + e.getMessage());
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        LOG.info("Sending warmup GS to Host " + hostIp + " is completed");
        return;
    }

    private String doSendGoalState(Goalstate.GoalStateV2 goalStateV2, CountDownLatch finishLatch, List<String> replies) {
        String hostIp = netwconfigmanagerGrpcServiceUrl;
        long start = System.currentTimeMillis();
        GrpcChannelStub channelStub = getOrCreateGrpcChannel(hostIp);
        long chan_established = System.currentTimeMillis();
        LOG.info("[doSendGoalState] Established channel, elapsed Time in milli seconds: " + (chan_established - start));
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = channelStub.stub;

        long stub_established = System.currentTimeMillis();
        LOG.info("[doSendGoalState] Established stub, elapsed Time after channel established in milli seconds: " + (stub_established - chan_established));

        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();
        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply reply) {
                LOG.info("Receive response from ACA@" + hostIp + " | " + reply.toString());
                result.put(hostIp, reply.getOperationStatusesList());
                if (reply.getOperationStatusesList().stream().filter(item -> item.getOperationStatus().equals(Common.OperationStatus.FAILURE)).collect(Collectors.toList()).size() > 0) {
                    replies.add(reply.toString());
                    while (finishLatch.getCount() > 0) {
                        finishLatch.countDown();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Receive error from ACA@" + hostIp + " |  " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                LOG.info("Complete receiving message from ACA@" + hostIp);
                finishLatch.countDown();
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        try {
            requestObserver.onNext(goalStateV2);
        } catch (RuntimeException e) {
            // Cancel RPC
            LOG.warn("[doSendGoalState] Sending GS, but error happened | " + e.getMessage());
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        LOG.info("Sending GS to Host " + hostIp + " is completed");

        // comment out onCompleted so that the same channel/stub and keep sending next time.
        requestObserver.onCompleted();
        return null;
    }

    private class GrpcChannelStub {
        public ManagedChannel channel;
        public GoalStateProvisionerGrpc.GoalStateProvisionerStub stub;

        public GrpcChannelStub(ManagedChannel channel, GoalStateProvisionerGrpc.GoalStateProvisionerStub stub) {
            this.channel = channel;
            this.stub = stub;
        }
    }

    private Goalstate.GoalStateV2.Builder getGoalState(Goalstate.GoalStateV2.Builder goalStateBuilder,  UnicastGoalStateV2 unicastGoalStateV2) {
        Goalstate.GoalStateV2 goalStateV2 = unicastGoalStateV2.getGoalState();
        Goalstate.HostResources.Builder hostResourceBuilder = Goalstate.HostResources.newBuilder();
        if (goalStateV2.getSubnetStatesCount() > 0) {
            goalStateV2.getSubnetStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType subnetResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.SUBNET)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(subnetResourceIdType);
            });
            goalStateBuilder.putAllSubnetStates(goalStateV2.getSubnetStatesMap());
        }

        if (goalStateV2.getDhcpStatesCount() > 0) {
            goalStateV2.getDhcpStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType dhcpResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.DHCP)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(dhcpResourceIdType);
            });
            goalStateBuilder.putAllDhcpStates(goalStateV2.getDhcpStatesMap());
        }

        if (goalStateV2.getPortStatesCount() > 0) {
            goalStateV2.getPortStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType portResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.PORT)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(portResourceIdType);
            });
            goalStateBuilder.putAllPortStates(goalStateV2.getPortStatesMap());
        }

        if (goalStateV2.getSecurityGroupStatesCount() > 0) {
            goalStateV2.getSecurityGroupStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType securityGroupResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.SECURITYGROUP)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(securityGroupResourceIdType);
            });
            goalStateBuilder.putAllSecurityGroupStates(goalStateV2.getSecurityGroupStatesMap());
        }

        if (goalStateV2.getNeighborStatesCount() > 0) {
            goalStateV2.getNeighborStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType neighborGroupResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.NEIGHBOR)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(neighborGroupResourceIdType);
            });
            goalStateBuilder.putAllNeighborStates(goalStateV2.getNeighborStatesMap());
        }

        if (goalStateV2.getRouterStatesCount() > 0) {
            goalStateV2.getRouterStatesMap().entrySet().forEach(entry -> {
                Goalstate.ResourceIdType routerResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.ROUTER)
                        .setId(unicastGoalStateV2.getHostIp() + "/" + entry.getKey())
                        .build();
                hostResourceBuilder.addResources(routerResourceIdType);


                if (goalStateBuilder.containsRouterStates(unicastGoalStateV2.getHostIp() + "/" + entry.getKey())) {
                   Router.RouterConfiguration.Builder routerConfigurationBuilder = goalStateBuilder.getRouterStatesMap().get(unicastGoalStateV2.getHostIp() + "/" + entry.getKey()).getConfiguration().toBuilder();
                   routerConfigurationBuilder.addAllSubnetRoutingTables(entry.getValue().getConfiguration().getSubnetRoutingTablesList());
                   Router.RouterState.Builder routerStateBuilder = goalStateBuilder.getRouterStatesMap().get(unicastGoalStateV2.getHostIp() + "/" + entry.getKey()).toBuilder();
                   routerStateBuilder.setConfiguration(routerConfigurationBuilder);
                } else {
                    goalStateBuilder.putRouterStates(unicastGoalStateV2.getHostIp() + "/" + entry.getKey(), entry.getValue());
                }
            });
        }

        if (goalStateV2.getVpcStatesCount() > 0) {
            goalStateV2.getVpcStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType vpcResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.VPC)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(vpcResourceIdType);
            });
            goalStateBuilder.putAllVpcStates(goalStateV2.getVpcStatesMap());
        }

        if (goalStateV2.getGatewayStatesCount() > 0) {
            goalStateV2.getGatewayStatesMap().keySet().forEach(key -> {
                Goalstate.ResourceIdType gatewayResourceIdType = Goalstate.ResourceIdType.newBuilder()
                        .setType(Common.ResourceType.GATEWAY)
                        .setId(key)
                        .build();
                hostResourceBuilder.addResources(gatewayResourceIdType);
            });
            goalStateBuilder.putAllGatewayStates(goalStateV2.getGatewayStatesMap());
        }

        if (goalStateBuilder.containsHostResources(unicastGoalStateV2.getHostIp())) {
            Goalstate.HostResources.Builder hostResourceBuilder1 = Goalstate.HostResources.newBuilder();
            hostResourceBuilder1.addAllResources(hostResourceBuilder.getResourcesList());
            hostResourceBuilder1.addAllResources(goalStateBuilder.getHostResourcesMap().get(unicastGoalStateV2.getHostIp()).getResourcesList());
            goalStateBuilder.putHostResources(unicastGoalStateV2.getHostIp(), hostResourceBuilder1.build());
        } else {
            goalStateBuilder.putHostResources(unicastGoalStateV2.getHostIp(), hostResourceBuilder.build());
        }
        return goalStateBuilder;
    }
}
