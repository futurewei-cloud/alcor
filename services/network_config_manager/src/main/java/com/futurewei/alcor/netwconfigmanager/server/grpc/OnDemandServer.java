package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.client.gRPC.GoalStateClientImpl;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

@Service
public class OnDemandServer implements NetworkConfigServer {


    private static final Logger logger = LoggerFactory.getLogger();

    private final int port;
    private final Server server;

//    @Autowired
//    private GoalStateClient grpcGoalStateClient;

    public OnDemandServer() {
        this.port = 9017;
        this.server = ServerBuilder.forPort(this.port)
                .addService(new OnDemandServerImpl())
                .build();
    }

    /**
     * Start a server with given port
     */
    @Override
    public void start() throws IOException {
        this.server.start();
        logger.log(Level.INFO, "OnDemandServer : Server started, listening on " + this.port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.log(Level.INFO, "*** shutting down gRPC server since JVM is shutting down");
                try {
                    OnDemandServer.this.stop();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "*** OnDemandServer shut down error");
                }
                logger.log(Level.INFO, "*** OnDemandServer shut down");
            }
        });
    }

    /**
     * Stop current server
     */
    @Override
    public void stop() throws InterruptedException {
        logger.log(Level.INFO,"OnDemandServer : Server stop, was listening on " + this.port);
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Override
    public void blockUntilShutdown() throws InterruptedException {
        logger.log(Level.INFO,"OnDemandServer : Server blockUntilShutdown, listening on " + this.port);
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    private static class OnDemandServerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

    }
}
