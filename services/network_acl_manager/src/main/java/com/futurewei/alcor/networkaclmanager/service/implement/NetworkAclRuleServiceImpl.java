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
package com.futurewei.alcor.networkaclmanager.service.implement;

import com.futurewei.alcor.networkaclmanager.exception.NetworkAclNotFound;
import com.futurewei.alcor.networkaclmanager.exception.NetworkAclRuleNotFound;
import com.futurewei.alcor.networkaclmanager.exception.RuleNumberOccupied;
import com.futurewei.alcor.networkaclmanager.repo.NetworkAclRepository;
import com.futurewei.alcor.networkaclmanager.service.NetworkAclRuleService;
import com.futurewei.alcor.web.entity.networkacl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NetworkAclRuleServiceImpl implements NetworkAclRuleService {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkAclRuleServiceImpl.class);

    @Autowired
    private NetworkAclRepository networkAclRepository;

    @Override
    public NetworkAclRuleEntity createNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        //Check if Network ACL exists
        String networkAclId = networkAclRuleEntity.getNetworkAclId();
        NetworkAclEntity networkAcl = networkAclRepository.getNetworkAcl(networkAclId);
        if (networkAcl == null) {
            throw new NetworkAclNotFound();
        }

        //Check if number has been occupied
        Integer number = networkAclRuleEntity.getNumber();
        if (networkAclRepository.getNetworkAclRulesByNumber(number) != null) {
            throw new RuleNumberOccupied();
        }

        networkAclRepository.addNetworkAclRule(networkAclRuleEntity);

        LOG.info("Update Network ACL Rule success, networkAclRuleWebJson: {}", networkAclRuleEntity);

        return networkAclRuleEntity;
    }

    @Override
    public NetworkAclRuleEntity updateNetworkAclRule(String networkAclRuleId, NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        NetworkAclRuleEntity oldNetworkAclRuleEntity = networkAclRepository.getNetworkAclRule(networkAclRuleId);
        if (oldNetworkAclRuleEntity == null) {
            throw new NetworkAclRuleNotFound();
        }

        boolean needUpdate = false;

        //Update rule number
        Integer newNumber = networkAclRuleEntity.getNumber();
        Integer oldNumber = oldNetworkAclRuleEntity.getNumber();
        if (newNumber != null && !newNumber.equals(oldNumber)) {
            if (networkAclRepository.getNetworkAclRulesByNumber(newNumber) != null) {
                throw new RuleNumberOccupied();
            }

            oldNetworkAclRuleEntity.setNumber(newNumber);
            needUpdate = true;
        }

        //Update ip prefix
        String newIpPrefix = networkAclRuleEntity.getIpPrefix();
        String oldIpPrefix = oldNetworkAclRuleEntity.getIpPrefix();
        if (newIpPrefix != null && !newIpPrefix.equals(oldIpPrefix)) {
            oldNetworkAclRuleEntity.setIpPrefix(newIpPrefix);
            needUpdate = true;
        }

        //Update action
        String newAction = networkAclRuleEntity.getAction();
        String oldAction = oldNetworkAclRuleEntity.getAction();
        if (newAction != null && !newAction.equals(oldAction)) {
            oldNetworkAclRuleEntity.setAction(newAction);
            needUpdate = true;
        }

        //Update direction
        String newDirection = networkAclRuleEntity.getDirection();
        String oldDirection = oldNetworkAclRuleEntity.getDirection();
        if (newDirection != null && !newDirection.equals(oldDirection)) {
            oldNetworkAclRuleEntity.setDirection(newDirection);
            needUpdate = true;
        }

        //Update etherType
        String newEtherType = networkAclRuleEntity.getEtherType();
        String oldEtherType = oldNetworkAclRuleEntity.getEtherType();
        if (newEtherType != null && !newEtherType.equals(oldEtherType)) {
            oldNetworkAclRuleEntity.setEtherType(newEtherType);
            needUpdate = true;
        }

        //Update protocol
        String newProtocol = networkAclRuleEntity.getProtocol();
        if (newProtocol != null) {
            if (Protocol.ICMP.getProtocol().equals(newProtocol)) {
                oldNetworkAclRuleEntity.setIcmpCode(networkAclRuleEntity.getIcmpCode());
                oldNetworkAclRuleEntity.setIcmpType(networkAclRuleEntity.getIcmpType());
            } else {
                oldNetworkAclRuleEntity.setPortRangeMax(networkAclRuleEntity.getPortRangeMax());
                oldNetworkAclRuleEntity.setPortRangeMin(networkAclRuleEntity.getPortRangeMin());
            }

            needUpdate = true;
        }

        if (needUpdate) {
            networkAclRepository.addNetworkAclRule(oldNetworkAclRuleEntity);
        }

        LOG.info("Update Network ACL Rule success, networkAclRuleEntity: {}", networkAclRuleEntity);

        return oldNetworkAclRuleEntity;
    }

    @Override
    public void deleteNetworkAclRule(String networkAclRuleId) throws Exception {
        NetworkAclRuleEntity networkAclRuleEntity = networkAclRepository.getNetworkAclRule(networkAclRuleId);
        if (networkAclRuleEntity == null) {
            throw new NetworkAclRuleNotFound();
        }

        networkAclRepository.deleteNetworkAclRule(networkAclRuleId);

        LOG.info("Delete Network ACL Rule success, networkAclId: {}", networkAclRuleId);
    }

    @Override
    public NetworkAclRuleEntity getNetworkAclRule(String networkAclRuleId) throws Exception {
        NetworkAclRuleEntity networkAclRuleEntity = networkAclRepository.getNetworkAclRule(networkAclRuleId);
        if (networkAclRuleEntity == null) {
            throw new NetworkAclRuleNotFound();
        }

        LOG.info("Get Network ACL Rule success, networkAclRuleEntity: {}", networkAclRuleEntity);

        return networkAclRuleEntity;
    }

    @Override
    public List<NetworkAclRuleEntity> listNetworkAclRule() throws Exception {
        List<NetworkAclRuleEntity> networkAclRuleEntities = new ArrayList<>();

        Map<String, NetworkAclRuleEntity> networkAclRuleEntityMap = networkAclRepository.getAllNetworkAclRules();
        if (networkAclRuleEntityMap == null) {
            return networkAclRuleEntities;
        }

        for (Map.Entry<String, NetworkAclRuleEntity> entry: networkAclRuleEntityMap.entrySet()) {
            networkAclRuleEntities.add(entry.getValue());
        }

        LOG.info("List Network ACL Rule success");

        return networkAclRuleEntities;
    }
}
