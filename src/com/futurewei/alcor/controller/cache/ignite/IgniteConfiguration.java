package com.futurewei.alcor.controller.cache.ignite;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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
@ComponentScan("com.futurewei.alcor.controller.cache")
@EntityScan("com.futurewei.alcor.controller.cache")
@ConditionalOnProperty(prefix = "ignite", name = "host")
public class IgniteConfiguration {
    private static final Logger logger = LoggerFactory.getLogger();

    @Value("${ignite.host}")
    private String host;

    @Value("${ignite.port}")
    private Integer port;

    @Bean
    public IgniteClient igniteClientInstance() {
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(host + ":" + String.valueOf(port));

        IgniteClient igniteClient = null;

        try {
            igniteClient = Ignition.startClient(cfg);
        }
        catch (ClientException e) {
            logger.log(Level.WARNING, "Start client failed:" + e.getMessage());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(igniteClient, "IgniteClient is null");

        return igniteClient;
    }
}
