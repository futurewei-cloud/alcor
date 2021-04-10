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

package com.futurewei.alcor.common.config;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import java.util.logging.Level;

//@Configuration
//@ComponentScan("com.futurewei.common.service")
//@EntityScan("com.futurewei.common.entity")
//@ConditionalOnProperty(prefix = "ignite", name = "host")
@Deprecated
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
