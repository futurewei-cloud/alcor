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
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service("grpcDataPlaneClientV2")
public class DataPlaneClientImplV2 implements DataPlaneClient<UnicastGoalStateV2, MulticastGoalStateV2> {

    private static DataPlaneClientImplV2 instance = null;

    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImplV2.class);

    private int hostAgentPort;

    private final ExecutorService executor;

    // each host_ip should have this amount of gRPC channels
    private int numberOfGrpcChannelPerHost;

    // when a channel is set up, send this amount of default GoalStates for warmup.
    private int numberOfWarmupsPerChannel;

    // prints out UUID and time, when sending a GoalState to any of the monitorHosts
    private ArrayList<String> monitorHosts;

    private ConcurrentHashMap<String, ArrayList<GrpcChannelStub>> hostIpGrpcChannelStubMap;

    @Override
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates) throws Exception {
        List<String> results = new ArrayList<>();
        for (UnicastGoalStateV2 unicastGoalState : unicastGoalStates) {
            results.add(
                    doSendGoalState(unicastGoalState)
            );
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

        this.monitorHosts = monitorHosts;
        LOG.info("Printing out all monitorHosts");
        for(String host : this.monitorHosts){
            LOG.info("Monitoring this host: " + host);
        }
        LOG.info("Done printing out all monitorHosts");
        this.numberOfGrpcChannelPerHost = globalConfig.numberOfGrpcChannelPerHost;
        this.numberOfWarmupsPerChannel = globalConfig.numberOfWarmupsPerChannel;
        this.hostAgentPort = 50001;

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
            warmUpChannelStub(channelStub, hostIp);
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

    private String doSendGoalState(UnicastGoalStateV2 unicastGoalState) {
        String hostIp = unicastGoalState.getHostIp();
        LOG.info("Setting up a channel to ACA on: " + hostIp);
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
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Receive error from ACA@" + hostIp + " |  " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                LOG.info("Complete receiving message from ACA@" + hostIp);
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        try {
            Goalstate.GoalStateV2 goalState = unicastGoalState.getGoalState();
            LOG.info("Sending GS to Host " + hostIp + " as follows | " + goalState.toString());
            requestObserver.onNext(goalState);
            if (unicastGoalState.getGoalState().getNeighborStatesCount() == 1 && monitorHosts.contains(hostIp)) {
                long sent_gs_time = System.currentTimeMillis();
                // If there's only one neighbor state and it is trying to send it to aca_node_one, the IP of which is now
                // hardcoded) this send goalstate action is probably caused by on-demand workflow, need to record when it
                // sends this goalState so what we can look into this and the ACA log to see how much time was spent.
                String neighbor_id = unicastGoalState.getGoalState().getNeighborStatesMap().keySet().iterator().next();
                LOG.info("Sending neighbor ID: " + neighbor_id + " at: " + sent_gs_time);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            LOG.warn("[doSendGoalState] Sending GS, but error happened | " + e.getMessage());
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        LOG.info("Sending GS to Host " + hostIp + " is completed");

        // comment out onCompleted so that the same channel/stub and keep sending next time.
        //        requestObserver.onCompleted();

//        shutdown(channel);

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
}
