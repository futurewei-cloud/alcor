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
package com.futurewei.alcor.common.db.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.internal.IgniteKernal;
import org.apache.ignite.internal.processors.cache.IgniteCacheProxyImpl;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;


public class MockIgniteServer {

    private static final String LOCAL_ADDRESS = "127.0.0.1";
    private static final int LISTEN_PORT = 11801;
    private static final int CLIENT_CONNECT_PORT = 10800;
    private static final int LISTEN_PORT_RANGE = 10;

    private static Ignite igniteServer = null;

    @BeforeClass
    public static void init() {
        if (igniteServer == null) {
            try {
                org.apache.ignite.configuration.IgniteConfiguration cfg = new org.apache.ignite.configuration.IgniteConfiguration();
                // make this ignite server isolated
                TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
                ipFinder.setAddresses(Collections.singletonList(LOCAL_ADDRESS + ":" +
                        LISTEN_PORT + ".." + (LISTEN_PORT+LISTEN_PORT_RANGE)));
                TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
                tcpDiscoverySpi.setIpFinder(ipFinder);
                tcpDiscoverySpi.setLocalAddress(LOCAL_ADDRESS);
                tcpDiscoverySpi.setLocalPort(LISTEN_PORT);
                tcpDiscoverySpi.setLocalPortRange(LISTEN_PORT_RANGE);
                cfg.setDiscoverySpi(tcpDiscoverySpi);
//                cfg.setPeerClassLoadingEnabled(true);

                ClientConnectorConfiguration clientConfig = new ClientConnectorConfiguration();
                clientConfig.setPort(CLIENT_CONNECT_PORT);
                cfg.setClientConnectorConfiguration(clientConfig);

                igniteServer = Ignition.start(cfg);

                //Properties properties = System.getProperties();
                //properties.setProperty("ignite.port", String.valueOf(ListenPort));
            } catch (IgniteException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @AfterClass
    public static void close() {
//        if(igniteServer != null){
//            igniteServer.close();
//            igniteServer = null;
//        }
    }

    public static Ignite getIgnite(){
        if(igniteServer == null){
            // if no need create a real ignite server, we return a mock Ignite client
            return new IgniteNodeClientMock();
        }
        return igniteServer;
    }

    /**
     * mock a {@link Ignite} class for unit test
     *
     */
    private static class IgniteNodeClientMock extends IgniteKernal {
        public IgniteNodeClientMock() {
        }

        @Override
        public <K, V> IgniteCache<K, V> getOrCreateCache(String cacheName) {
            return new IgniteCacheProxyImpl();
        }

        @Override
        public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> var1) {
            return new IgniteCacheProxyImpl();
        }
    }

}
