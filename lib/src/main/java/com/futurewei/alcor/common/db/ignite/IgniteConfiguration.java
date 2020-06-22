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

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignite;
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

import java.util.Collections;
import java.util.logging.Level;

@Configuration
@ComponentScan("com.futurewei.alcor.common.db")
@EntityScan("com.futurewei.alcor.common.db")
@ConditionalOnProperty(prefix = "ignite", name = "host")
public class IgniteConfiguration {
    private static final Logger logger = LoggerFactory.getLogger();

    @Value("${ignite.host}")
    private String host;

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


    //@Bean
    public IgniteClient igniteClientInstance() {
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(host + ":" + port);

        if (keyStorePath != null && keyStorePassword != null &&
                trustStorePath != null && trustStorePassword != null) {
            cfg.setSslClientCertificateKeyStorePath(keyStorePath)
                    .setSslClientCertificateKeyStorePassword(keyStorePassword)
                    .setSslTrustCertificateKeyStorePath(trustStorePath)
                    .setSslTrustCertificateKeyStorePassword(trustStorePassword);
        }

        IgniteClient igniteClient = null;

        try {
            igniteClient = Ignition.startClient(cfg);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Start client failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        //Assert.notNull(igniteClient, "IgniteClient is null");

        return igniteClient;
    }

    @Bean
    public Ignite igniteInstance() {
        org.apache.ignite.configuration.IgniteConfiguration cfg =
                new org.apache.ignite.configuration.IgniteConfiguration();

        // The node will be started as a client node.
        cfg.setClientMode(true);

        // Classes of custom Java logic will be transferred over the wire from this app.
        cfg.setPeerClassLoadingEnabled(true);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList(host + ":" + port));
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setJoinTimeout(1000);
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
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Start client failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        // Assert.notNull(client, "Ignite client is null");

        return client;
    }
}
