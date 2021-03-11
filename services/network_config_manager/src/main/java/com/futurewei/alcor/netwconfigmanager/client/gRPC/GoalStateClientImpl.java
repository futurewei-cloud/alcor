package com.futurewei.alcor.netwconfigmanager.client.gRPC;

import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.config.Config;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service("grpcGoalStateClient")
public class GoalStateClientImpl implements GoalStateClient {
    private static final Logger LOG = LoggerFactory.getLogger(GoalStateClientImpl.class);

    private int grpcPort;

    private final ExecutorService executor;

    @Autowired
    public GoalStateClientImpl(Config globalConfig) {
        this.grpcPort = globalConfig.targetHostPort;
        this.executor = new ThreadPoolExecutor(globalConfig.grpcMinThreads,
                globalConfig.grpcMaxThreads,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory(globalConfig.grpThreadsName));
    }

    @Override
    public List<String> sendGoalStates(Map<String, HostGoalState> hostGoalStates) throws Exception {
        List<Future<HostGoalState>>
                futures = new ArrayList<>(hostGoalStates.size());

        for (HostGoalState hostGoalState: hostGoalStates.values()) {
            Future<HostGoalState> future =
                    executor.submit(() -> {
                        try {
                            doSendGoalState(hostGoalState);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return hostGoalState;
                        }

                        return new HostGoalState();
                    });

            futures.add(future);
        }

        //Handle all failed hosts
        return futures.parallelStream().filter(Objects::nonNull).map(future -> {
            try {
                return future.get().getHostIp();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }).collect(Collectors.toList());
    }

    private void doSendGoalState(HostGoalState hostGoalState) throws InterruptedException{

        String hostIp = hostGoalState.getHostIp();
        Goalstate.GoalStateV2 goalState = hostGoalState.getGoalState();

        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostIp, this.grpcPort)
                .usePlaintext()
                .build();
        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub =
                GoalStateProvisionerGrpc.newBlockingStub(channel);

        Goalstateprovisioner.GoalStateOperationReply reply = null;
//            = blockingStub.pushNetworkResourceStates();
        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> statuses =
                reply.getOperationStatusesList();

        result.put(hostIp, statuses);

        shutdown(channel);
    }

    private void shutdown(ManagedChannel channel) {
        try {
            channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Timed out forcefully shutting down connection: {}", e.getMessage());
        }
    }
}
