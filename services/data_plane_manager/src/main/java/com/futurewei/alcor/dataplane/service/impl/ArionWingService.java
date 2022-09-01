package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.dataplane.cache.ArionWingCache;
import com.futurewei.alcor.dataplane.entity.ArionWing;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.schema.Gateway;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.ishugaliy.allgood.consistent.hash.ConsistentHash;
import org.ishugaliy.allgood.consistent.hash.HashRing;
import org.ishugaliy.allgood.consistent.hash.hasher.DefaultHasher;
import org.ishugaliy.allgood.consistent.hash.node.SimpleNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ArionWingService {

    @Autowired
    private ArionWingCache arionWingCache;

    private ConsistentHash<SimpleNode> ring;

    public ArionWingService () {
        ring = HashRing.<SimpleNode>newBuilder()
                .name("file_cache_hash_ring")       // set hash ring name
                .hasher(DefaultHasher.METRO_HASH)   // hash function to distribute partitions
                .partitionRate(10)                  // number of partitions per node
                .nodes(Arrays.asList())       // initial nodes set
                .build();
    }

    public void buildArionGatewayState (NetworkConfiguration networkConfiguration, UnicastGoalStateV2 unicastGoalStateV2) throws CacheException {
        for (InternalSubnetEntity internalSubnetEntity : networkConfiguration.getSubnets()) {
            Gateway.GatewayState.Builder gatewayStateBuilder = Gateway.GatewayState.newBuilder();
            int vni = internalSubnetEntity.getTunnelId().intValue();
            String key = internalSubnetEntity.getId();
            getArionWings();
            String group = getArionGroup(key);
            Map<String, Object[]> queryParams =  new HashMap<>();
            Object[] value = new Object[1];
            value[0] = group;
            queryParams.put("group", value);
            Collection<ArionWing> arionWings = arionWingCache.getAllSubnetPorts(queryParams).values();
            for (ArionWing arionWing : arionWings) {
                Gateway.GatewayConfiguration.destination.Builder builder = Gateway.GatewayConfiguration.destination.newBuilder();
                builder.setIpAddress(arionWing.getIp());
                builder.setMacAddress(arionWing.getMac());
                gatewayStateBuilder.getConfigurationBuilder().addDestinations(builder);
            }
            String id = UUID.randomUUID().toString();
            gatewayStateBuilder.getConfigurationBuilder().setId(id);
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setVni(vni);
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setSubnetId(internalSubnetEntity.getId());
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setVpcId(internalSubnetEntity.getVpcId());
            gatewayStateBuilder.getConfigurationBuilder().setGatewayType(Gateway.GatewayType.ARION);
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setPortInbandOperation(8300);
            unicastGoalStateV2.getGoalStateBuilder().putGatewayStates(id, gatewayStateBuilder.build());
        }
    }

    public String getArionGroup (String subnetId) {
        Optional<SimpleNode> group = ring.locate(subnetId);
        return group.get().getKey();
    }

    public void getArionWings () throws CacheException {
        arionWingCache.getAllArionGroup().values().stream().map(item -> ring.remove(SimpleNode.of(item.getGroupName())));
        Set<SimpleNode> keysInCache = arionWingCache.getAllArionGroup().values().stream().map(item -> SimpleNode.of(item.getGroupName())).collect(Collectors.toSet());
        ring.addAll(keysInCache);

    }

    public void createArionWingGroup (String resourcdId) throws CacheException {
        arionWingCache.insertArionGroup(resourcdId);
    }

    public void deleteArionWingGroup (String resourcdId) throws CacheException {
        arionWingCache.deleteArionGroup(resourcdId);
    }

    public void createArionWing(ArionWing arionWing) throws CacheException {
        arionWingCache.insertArionWing(arionWing);
    }

    public void updateArionWing(ArionWing arionWing) throws CacheException {
        arionWingCache.insertArionWing(arionWing);
    }

    public void deleteArionWing (String resourceId) throws CacheException {
        arionWingCache.deleteArionWing(resourceId);
    }
}
