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
package com.futurewei.alcor.dataplane.client.grpc;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.entity.HostGoalState;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.catalina.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class DataPlaneClientImpl implements DataPlaneClient {

    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    private int grpcPort;

    @Autowired
    private Config globalConfig;

    private final ExecutorService executor;

    @Autowired
    public DataPlaneClientImpl() {
        this.grpcPort = globalConfig.port;
        executor = new ThreadPoolExecutor(globalConfig.grpcMinThreads,
                globalConfig.grpcMaxThreads, 50, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory(globalConfig.grpThreadsName));
    }

    @Override
    public Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    createGoalState(Goalstate.GoalState goalState, String hostIp) throws Exception {
        return sendGoalState(goalState, hostIp);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    createGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    updateGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    deleteGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    private List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    sendGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        List<Future<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>>
                futures = new ArrayList<>(hostGoalStates.size());
        for (HostGoalState hostGoalState: hostGoalStates) {
            Future<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> future =
                    executor.submit(() -> {
                try {
                    return sendGoalState(hostGoalState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
            futures.add(future);
        }
        return futures.parallelStream().map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    private Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    sendGoalState(HostGoalState hostGoalState) throws InterruptedException {
        return sendGoalState(hostGoalState.getGoalState(), hostGoalState.getHostIp());
    }

    private Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    sendGoalState(Goalstate.GoalState goalState, String hostIp) throws InterruptedException {

        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();

        ManagedChannel channel = newChannel(hostIp, grpcPort);
        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub =
                GoalStateProvisionerGrpc.newBlockingStub(channel);
        Goalstateprovisioner.GoalStateOperationReply reply =
                blockingStub.pushNetworkResourceStates(goalState);
        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> statuses =
                reply.getOperationStatusesList();

        result.put(hostIp, statuses);

        shutdown(channel);
        return result;
    }

    private List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    asyncSendGoalStates(List<HostGoalState> hostGoalStates) {
        List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> result =
                new ArrayList<>();

        CountDownLatch finished = new CountDownLatch(hostGoalStates.size());

        for (HostGoalState hostGoalState: hostGoalStates) {
            asyncSendGoalState(hostGoalState, new StreamObserver<Goalstateprovisioner.GoalStateOperationReply>() {
                @Override
                public void onNext(Goalstateprovisioner.GoalStateOperationReply value) {
                    result.add(Collections.singletonMap(hostGoalState.getHostIp(), value.getOperationStatusesList()));
                }

                @Override
                public void onError(Throwable t) {
                    finished.countDown();
                }

                @Override
                public void onCompleted() {
                    finished.countDown();
                }
            });
        }

        try {
            finished.await(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("goalState can not finished");
        }
        return result;
    }

    private void
    asyncSendGoalState(HostGoalState hostGoalState,
                       StreamObserver<Goalstateprovisioner.GoalStateOperationReply> observer) {
        asyncSendGoalState(hostGoalState.getGoalState(), hostGoalState.getHostIp(), observer);
    }

    private void
    asyncSendGoalState(Goalstate.GoalState goalState, String hostIp,
                       StreamObserver<Goalstateprovisioner.GoalStateOperationReply> observer) {
        ManagedChannel channel = newChannel(hostIp, grpcPort);
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub =
                GoalStateProvisionerGrpc.newStub(channel).withExecutor(executor);
        asyncStub.pushNetworkResourceStates(goalState, observer);
    }

    private ManagedChannel newChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    private void shutdown(ManagedChannel channel) {
        try {
            channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Timed out forcefully shutting down connection: {}", e.getMessage());
        }
    }
}
