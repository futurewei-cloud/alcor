package com.futurewei.alcor.netwconfigmanager.server;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public interface NetworkConfigServer {

    /**
     * Start a server with given port
     */
    void start(int port) throws IOException;

    /**
     * Stop current server
     */
    void stop() throws InterruptedException;

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    void blockUntilShutdown() throws InterruptedException;
}
