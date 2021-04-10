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
import java.util.Objects;
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
        // if no need create a real ignite server, we return a mock Ignite client
        return Objects.requireNonNullElseGet(igniteServer, IgniteNodeClientMock::new);
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
        public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg) {
            return new IgniteCacheProxyImpl();
        }
    }

}
