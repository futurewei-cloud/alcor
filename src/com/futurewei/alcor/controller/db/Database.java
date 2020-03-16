package com.futurewei.alcor.controller.db;

import com.futurewei.alcor.controller.db.ignite.IgniteCache;
import com.futurewei.alcor.controller.db.redis.RedisCache;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@ComponentScan
@Component
public class Database {
    @Autowired(required = false)
    private IgniteClient igniteClient;

    @Autowired
    RedisTemplate<String, VpcState> vpcTemplate;

    @Autowired
    RedisTemplate<String, SubnetState> subnetTemplate;

    @Autowired
    RedisTemplate<String, PortState> portTemplate;

    @Bean
    Database databaseInstance() {
        return new Database();
    }

    private ICache getIgniteCache(String name) {
        return new IgniteCache<>(igniteClient, name);
    }

    public ICache getVpcCache(String name) {
        if (igniteClient != null) {
            return getIgniteCache(name);
        }

        return new RedisCache<>(vpcTemplate, name);
    }

    public ICache getSubnetCache(String name) {
        if (igniteClient != null) {
            return getIgniteCache(name);
        }

        return new RedisCache<>(subnetTemplate, name);
    }

    public ICache getPortCache(String name) {
        if (igniteClient != null) {
            return getIgniteCache(name);
        }

        return new RedisCache<>(portTemplate, name);
    }
}
