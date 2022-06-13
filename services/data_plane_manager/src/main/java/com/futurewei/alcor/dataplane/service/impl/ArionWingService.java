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
            String subnet = internalSubnetEntity.getCidr();
            String key = String.valueOf(vni) + "-" + subnet;
            getArionWings();
            Optional<SimpleNode> group = ring.locate(key);
            Map<String, Object[]> queryParams =  new HashMap<>();
            Object[] value = new Object[1];
            value[0] = group.get().getKey();
            queryParams.put("group", value);
            Collection<ArionWing> arionWings = arionWingCache.getAllSubnetPorts(queryParams).values();
            for (ArionWing arionWing : arionWings) {
                Gateway.GatewayConfiguration.destination.Builder builder = Gateway.GatewayConfiguration.destination.newBuilder();
                builder.setIpAddress(arionWing.getIp());
                builder.setMacAddress(arionWing.getMac());
                gatewayStateBuilder.getConfigurationBuilder().addDestinations(builder);
            }
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setVni(vni);
            gatewayStateBuilder.getConfigurationBuilder().getArionInfoBuilder().setSubnetId(subnet);
            unicastGoalStateV2.getGoalStateBuilder().putGatewayStates(key, gatewayStateBuilder.build());
        }
    }

    public String getArionGroup (int vni, String subnet) {
        String key = String.valueOf(vni) + "-" + subnet;
        Optional<SimpleNode> group = ring.locate(key);
        return group.get().getKey();
    }


    public void getArionWings () throws CacheException {
        Set<String> keys = new HashSet<>();
        if (!ring.getNodes().isEmpty()) {
            keys = ring.getNodes().stream().map(item -> item.getKey()).collect(Collectors.toSet());
        }
        Set<String> keysInCache = arionWingCache.getAllArionGroup().keySet();
        if (!keys.equals(keysInCache)) {
            keys.retainAll(keysInCache);
            for (SimpleNode node : ring.getNodes()) {
                if (!keys.contains(node.getKey())) {
                    ring.remove(node);
                }
            }
            keysInCache.removeAll(keys);
            for (var key : keysInCache) {
                ring.add(SimpleNode.of(key));
            }
        }
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
