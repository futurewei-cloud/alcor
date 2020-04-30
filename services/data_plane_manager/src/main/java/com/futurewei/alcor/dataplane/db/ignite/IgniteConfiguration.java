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

package com.futurewei.alcor.dataplane.db.ignite;

import com.futurewei.alcor.dataplane.logging.Logger;
import com.futurewei.alcor.dataplane.logging.LoggerFactory;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.Assert;

import java.util.logging.Level;

@Configuration
@ComponentScan("com.futurewei.alcor.dataplane.db")
@EntityScan("com.futurewei.alcor.dataplane.db")
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

    @Bean
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

        Assert.notNull(igniteClient, "IgniteClient is null");

        return igniteClient;
    }
}
