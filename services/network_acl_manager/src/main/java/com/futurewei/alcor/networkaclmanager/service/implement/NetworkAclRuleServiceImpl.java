/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
import java.util.UUID;

@Service
public class NetworkAclRuleServiceImpl implements NetworkAclRuleService {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkAclRuleServiceImpl.class);

    @Autowired
    private NetworkAclRepository networkAclRepository;

    @Override
    public NetworkAclRuleEntity createNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        //Check if Network ACL exists
        String networkAclId = networkAclRuleEntity.getNetworkAclId();
        NetworkAclEntity networkAclEntity = networkAclRepository.getNetworkAcl(networkAclId);
        if (networkAclEntity == null) {
            throw new NetworkAclNotFound();
        }

        //Generate uuid for networkAclRuleEntity
        if (networkAclRuleEntity.getId() == null) {
            networkAclRuleEntity.setId(UUID.randomUUID().toString());
        }

        //Check if number has been occupied
        Integer number = networkAclRuleEntity.getNumber();
        if (!networkAclRepository.getNetworkAclRulesByNumber(number).isEmpty()) {
            throw new RuleNumberOccupied();
        }

        networkAclRepository.addNetworkAclRule(networkAclRuleEntity);

        LOG.info("Update Network ACL Rule success, networkAclRuleWebJson: {}", networkAclRuleEntity);

        return networkAclRuleEntity;
    }

    @Override
    public List<NetworkAclRuleEntity> createNetworkAclRuleBulk(List<NetworkAclRuleEntity> networkAclRuleEntities) throws Exception {
        for (NetworkAclRuleEntity networkAclRuleEntity: networkAclRuleEntities) {
            NetworkAclEntity networkAclEntity = networkAclRepository.
                    getNetworkAcl(networkAclRuleEntity.getNetworkAclId());
            if (networkAclEntity == null) {
                throw new NetworkAclNotFound();
            }

            if (networkAclRuleEntity.getId() == null) {
                networkAclRuleEntity.setId(UUID.randomUUID().toString());
            }

            Integer number = networkAclRuleEntity.getNumber();
            if (!networkAclRepository.getNetworkAclRulesByNumber(number).isEmpty()) {
                throw new RuleNumberOccupied();
            }
        }

        networkAclRepository.addNetworkAclRuleBulk(networkAclRuleEntities);

        return networkAclRuleEntities;
    }

    @Override
    public NetworkAclRuleEntity updateNetworkAclRule(String networkAclRuleId, NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        NetworkAclRuleEntity oldNetworkAclRuleEntity = networkAclRepository.getNetworkAclRule(networkAclRuleId);
        if (oldNetworkAclRuleEntity == null) {
            throw new NetworkAclRuleNotFound();
        }

        boolean needUpdate = false;

        //Update name
        if (networkAclRuleEntity.getName() != null) {
            if (!networkAclRuleEntity.getName().equals(oldNetworkAclRuleEntity.getName())) {
                oldNetworkAclRuleEntity.setName(networkAclRuleEntity.getName());
                needUpdate = true;
            }
        }

        //Update description
        if (networkAclRuleEntity.getDescription() != null) {
            if (!networkAclRuleEntity.getDescription().equals(oldNetworkAclRuleEntity.getDescription())) {
                oldNetworkAclRuleEntity.setDescription(networkAclRuleEntity.getDescription());
                needUpdate = true;
            }
        }

        //Update network acl id
        String newNetworkAclId = networkAclRuleEntity.getNetworkAclId();
        String oldNetworkAclId = oldNetworkAclRuleEntity.getNetworkAclId();
        if (newNetworkAclId != null && !newNetworkAclId.equals(oldNetworkAclId)) {
            if (networkAclRepository.getNetworkAcl(newNetworkAclId) == null) {
                throw new NetworkAclNotFound();
            }

            oldNetworkAclRuleEntity.setNetworkAclId(newNetworkAclId);
            needUpdate = true;
        }

        //Update rule number
        Integer newNumber = networkAclRuleEntity.getNumber();
        Integer oldNumber = oldNetworkAclRuleEntity.getNumber();
        if (newNumber != null && !newNumber.equals(oldNumber)) {
            if (!networkAclRepository.getNetworkAclRulesByNumber(newNumber).isEmpty()) {
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
        String oldProtocol = oldNetworkAclRuleEntity.getProtocol();
        if (newProtocol != null && !newProtocol.equals(oldProtocol)) {
            oldNetworkAclRuleEntity.setProtocol(newProtocol);
            needUpdate = true;
        }

        //Update icmp type
        Integer newIcmpType = networkAclRuleEntity.getIcmpType();
        Integer oldIcmpType = oldNetworkAclRuleEntity.getIcmpType();
        if (newIcmpType != null && !newIcmpType.equals(oldIcmpType)) {
            oldNetworkAclRuleEntity.setIcmpType(networkAclRuleEntity.getIcmpType());
            needUpdate = true;
        }

        //Update icmp code
        Integer newIcmpCode = networkAclRuleEntity.getIcmpCode();
        Integer oldIcmpCode = oldNetworkAclRuleEntity.getIcmpCode();
        if (newIcmpCode != null && !newIcmpCode.equals(oldIcmpCode)) {
            oldNetworkAclRuleEntity.setIcmpCode(networkAclRuleEntity.getIcmpCode());
            needUpdate = true;
        }

        //Update port range max
        Integer newPortRangeMax = networkAclRuleEntity.getPortRangeMax();
        Integer oldPortRangeMax = oldNetworkAclRuleEntity.getPortRangeMax();
        if (newPortRangeMax != null && !newPortRangeMax.equals(oldPortRangeMax)) {
            oldNetworkAclRuleEntity.setPortRangeMax(networkAclRuleEntity.getPortRangeMax());
            needUpdate = true;
        }

        //Update port range min
        Integer newRangeMin = networkAclRuleEntity.getPortRangeMin();
        Integer oldRangeMin = oldNetworkAclRuleEntity.getPortRangeMin();
        if (newRangeMin != null && !newRangeMin.equals(oldRangeMin)) {
            oldNetworkAclRuleEntity.setPortRangeMin(networkAclRuleEntity.getPortRangeMin());
            needUpdate = true;
        }

        if (needUpdate) {
            networkAclRepository.addNetworkAclRule(oldNetworkAclRuleEntity);
        }

        LOG.info("Update Network ACL Rule success, networkAclRuleEntity: {}", networkAclRuleEntity);

        return oldNetworkAclRuleEntity;
    }

    @Override
    public List<NetworkAclRuleEntity> updateNetworkAclRuleBulk(List<NetworkAclRuleEntity> networkAclRuleEntities) throws Exception {
        List<NetworkAclRuleEntity> result = new ArrayList<>();
        for (NetworkAclRuleEntity networkAclRuleEntity: networkAclRuleEntities) {
            result.add(updateNetworkAclRule(networkAclRuleEntity.getId(), networkAclRuleEntity));
        }

        return result;
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
