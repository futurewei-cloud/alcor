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
package com.futurewei.alcor.networkaclmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.web.entity.networkacl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class NetworkAclRepository {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkAclRepository.class);

    private ICache<String, NetworkAclEntity> networkAclCache;
    private ICache<String, NetworkAclRuleEntity> networkAclRuleCache;

    public NetworkAclRepository() {

    }

    public NetworkAclRepository(ICache<String, NetworkAclEntity> networkAclCache, ICache<String, NetworkAclRuleEntity> networkAclRuleCache) {
        this.networkAclCache = networkAclCache;
        this.networkAclRuleCache = networkAclRuleCache;
    }

    @Autowired
    public NetworkAclRepository(CacheFactory cacheFactory) {
        networkAclCache = cacheFactory.getCache(NetworkAclEntity.class);
        networkAclRuleCache = cacheFactory.getCache(NetworkAclRuleEntity.class);
    }

    private NetworkAclRuleEntity createDefaultNetworkAclRule(String ipPrefix, String direction) {
        NetworkAclRuleEntity networkAclRuleEntity = new NetworkAclRuleEntity();
        networkAclRuleEntity.setId(UUID.randomUUID().toString());
        networkAclRuleEntity.setNumber(NetworkAclRuleEntity.NUMBER_MAX_VALUE);
        networkAclRuleEntity.setIpPrefix(ipPrefix);
        networkAclRuleEntity.setProtocol(Protocol.ALL.getProtocol());
        networkAclRuleEntity.setDirection(direction);
        networkAclRuleEntity.setAction(Action.DENY.getAction());

        return networkAclRuleEntity;
    }

    @PostConstruct
    private void init() throws Exception {
        try (Transaction tx = networkAclCache.getTransaction().start()) {
            List<NetworkAclRuleEntity> networkAclRules = getDefaultNetworkAclRules();

            if (networkAclRules == null) {
                List<String> ipPrefixes = Arrays.asList(NetworkAclRuleEntity.DEFAULT_IPV4_PREFIX,
                        NetworkAclRuleEntity.DEFAULT_IPV6_PREFIX);
                Map<String, NetworkAclRuleEntity> networkAclRuleEntityMap = new HashMap<>();

                for (String ipPrefix: ipPrefixes) {
                    List<Direction> directions = Arrays.asList(Direction.INGRESS,
                            Direction.EGRESS);

                    for (Direction direction : directions) {
                        NetworkAclRuleEntity networkAclRuleEntity =
                                createDefaultNetworkAclRule(ipPrefix, direction.getDirection());
                        networkAclRuleEntityMap.put(networkAclRuleEntity.getId(), networkAclRuleEntity);
                    }
                }

                networkAclRuleCache.putAll(networkAclRuleEntityMap);
            }

            tx.commit();
        }

        LOG.info("NetworkAclRepository init done");
    }

    public void addNetworkAcl(NetworkAclEntity networkAclEntity) throws CacheException {
        LOG.debug("Add Network ACL:{}", networkAclEntity);

        networkAclCache.put(networkAclEntity.getId(), networkAclEntity);
    }

    public void deleteNetworkAcl(String networkAclId) throws CacheException {
        LOG.debug("Delete Network ACL, Network ACL Id:{}", networkAclId);
        networkAclCache.remove(networkAclId);
    }

    public NetworkAclEntity getNetworkAcl(String networkAclId) throws CacheException {
        NetworkAclEntity networkAclEntity = networkAclCache.get(networkAclId);
        if (networkAclEntity == null) {
            return null;
        }

        List<NetworkAclRuleEntity> networkAclRules =
                getNetworkAclRulesByNetworkAclId(networkAclEntity.getId());
        if (networkAclRules == null) {
            networkAclRules = getDefaultNetworkAclRules();
        }

        networkAclEntity.setNetworkAclRuleEntities(networkAclRules);

        return networkAclEntity;
    }

    public Map<String, NetworkAclEntity> getAllNetworkAcls() throws CacheException {
        Map<String, NetworkAclEntity> networkAclEntityMap = networkAclCache.getAll();
        if (networkAclEntityMap == null) {
            return null;
        }

        for (Map.Entry<String, NetworkAclEntity> entry: networkAclEntityMap.entrySet()) {
            List<NetworkAclRuleEntity> networkAclRules = getNetworkAclRulesByNetworkAclId(entry.getKey());
            if (networkAclRules == null) {
                networkAclRules = getDefaultNetworkAclRules();
            }

            entry.getValue().setNetworkAclRuleEntities(networkAclRules);
        }

        return networkAclEntityMap;
    }

    public void addNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        LOG.debug("Add Network ACL Rule:{}", networkAclRuleEntity);

        networkAclRuleCache.put(networkAclRuleEntity.getId(), networkAclRuleEntity);
    }

    public void deleteNetworkAclRule(String networkAclRuleId) throws CacheException {
        LOG.debug("Delete Network ACL Rule, Network ACL Rule Id:{}", networkAclRuleId);
        networkAclRuleCache.remove(networkAclRuleId);
    }

    public NetworkAclRuleEntity getNetworkAclRule(String networkAclRuleId) throws CacheException {
        return networkAclRuleCache.get(networkAclRuleId);
    }

    public List<NetworkAclRuleEntity> getNetworkAclRulesByNumber(Integer number) throws CacheException {
        //FIXME: not support yet
        return null;
    }

    public List<NetworkAclRuleEntity> getDefaultNetworkAclRules() throws CacheException {
        return getNetworkAclRulesByNumber(NetworkAclRuleEntity.NUMBER_MAX_VALUE);
    }

    public List<NetworkAclRuleEntity> getNetworkAclRulesByNetworkAclId(String networkAclId) throws CacheException {
        //FIXME: not support yet,

        return null;
    }

    public Map<String, NetworkAclRuleEntity> getAllNetworkAclRules() throws CacheException {
        return networkAclRuleCache.getAll();
    }
}
