/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.ICacheFactory;
import org.apache.ignite.client.ThinClientKubernetesAddressFinder;
import org.apache.ignite.kubernetes.configuration.KubernetesConnectionConfiguration;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.logging.Level;

@Configuration
@ComponentScan("com.futurewei.alcor.common.db")
@EntityScan("com.futurewei.alcor.common.db")
@ConditionalOnProperty(prefix = "ignite", name = "host")
public class IgniteConfiguration {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final int JOIN_TIMEOUT = 1000;

    private static final String THICK_CLIENT = "ThickClient";

    @Value("${ignite.host}")
    private String host;

    @Value("${ignite.kubeNamespace:localhost}")
    private String kubeNamespace;

    @Value("${ignite.kubeServiceName:ignite}")
    private String kubeServiceName;

    @Value("${ignite.port}")
    private Integer port;

    @Value("${ignite.key-store-path:#{null}}")
    private String keyStorePath;

    @Value("${ignite.key-store-password:#{null}}")
    private String keyStorePassword;

    @Value("${ignite.trust-store-path:#{null}}")
    private String trustStorePath;

    @Value("${ignite.trust-store-password:#{null}}")
    private String trustStorePassword;

    @Value("${ignite.thin.client.enable: #{true}}")
    private boolean thinClientEnable;

    @Value("${lock.try.interval:10}")
    private int tryLockInterval;

    @Value("${lock.expire.time:120}")
    private int expireTime;

    @Bean
    @Primary
    public ICacheFactory igniteClientFactoryInstance(){
        if(thinClientEnable){

            return new IgniteClientCacheFactory(this.getThinIgniteClient(),
                    this.tryLockInterval, this.expireTime);
        }
        return new IgniteCacheFactory(this.getIgniteClient(THICK_CLIENT),
                this.tryLockInterval, this.expireTime);
    }

    private IgniteClient getThinIgniteClient() {
        ClientConfiguration cfg = new ClientConfiguration();

        /***
         * With partition awareness in place, the thin client can directly route queries and operations to the primary nodes that own the data required for the queries.
         * This eliminates the bottleneck, allowing the application to scale more easily.
         */
        KubernetesConnectionConfiguration kcfg = new KubernetesConnectionConfiguration();
        kcfg.setNamespace(kubeNamespace);
        kcfg.setServiceName(kubeServiceName);
        kcfg.setDiscoveryPort(port);

        ClientConfiguration ccfg = new ClientConfiguration();
        ccfg.setAddressesFinder(new ThinClientKubernetesAddressFinder(kcfg));

        cfg.setAddresses(host + ":" + port)
                .setPartitionAwarenessEnabled(true);

        if (keyStorePath != null && keyStorePassword != null &&
                trustStorePath != null && trustStorePassword != null) {
            cfg.setSslClientCertificateKeyStorePath(keyStorePath)
                    .setSslClientCertificateKeyStorePassword(keyStorePassword)
                    .setSslTrustCertificateKeyStorePath(trustStorePath)
                    .setSslTrustCertificateKeyStorePassword(trustStorePassword);
        }

        IgniteClient igniteClient = null;

        try {
            if (host.equals("localhost")) {
                igniteClient = Ignition.startClient(cfg);
            } else {
                igniteClient = Ignition.startClient(ccfg);
            }

        } catch (ClientException e) {
            logger.log(Level.WARNING, "Start client failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(igniteClient, "IgniteClient is null");

        return igniteClient;
    }

    private Ignite getIgniteClient(String instanceName) {
        org.apache.ignite.configuration.IgniteConfiguration cfg =
                new org.apache.ignite.configuration.IgniteConfiguration();

        // Set client name to allow multiple clients
        cfg.setIgniteInstanceName(instanceName);

        // The node will be started as a client node.
        cfg.setClientMode(true);

        // Classes of custom Java logic will be transferred over the wire from this app.
        cfg.setPeerClassLoadingEnabled(true);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList(host + ":" + port));
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setJoinTimeout(JOIN_TIMEOUT);
        tcpDiscoverySpi.setStatisticsPrintFrequency(0);
        cfg.setDiscoverySpi(tcpDiscoverySpi);

        if (keyStorePath != null && keyStorePassword != null) {
            SslContextFactory factory = new SslContextFactory();
            factory.setKeyStoreFilePath(keyStorePath);
            factory.setKeyStorePassword(keyStorePassword.toCharArray());
            if(trustStorePath != null && trustStorePassword != null) {
                factory.setTrustStoreFilePath(trustStorePath);
                factory.setTrustStorePassword(trustStorePassword.toCharArray());
            }else{
                factory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            }

            cfg.setSslContextFactory(factory);
        }

        Ignite client = null;

        try {
            client = Ignition.start(cfg);
        } catch (IgniteException e) {
            logger.log(Level.WARNING, "Start client failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(client, "Ignite client is null");

        return client;
    }

}
