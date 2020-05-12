package com.futurewei.alcor.common.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class MockIgniteConfiguration {

    private static Ignite igniteServer = null;

    private static int ListenPort = 10801;


    @BeforeClass
    public static void init() {
        if (igniteServer == null) {
            try {

                ClientConnectorConfiguration clientConfig = new ClientConnectorConfiguration();
                clientConfig.setPort(ListenPort);
                org.apache.ignite.configuration.IgniteConfiguration cfg = new org.apache.ignite.configuration.IgniteConfiguration();
                cfg.setClientConnectorConfiguration(clientConfig);
                igniteServer = Ignition.start(cfg);

            } catch (IgniteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @AfterClass
    public static void close() { }

}
