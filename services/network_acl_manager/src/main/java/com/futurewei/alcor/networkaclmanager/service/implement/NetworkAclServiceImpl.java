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
import com.futurewei.alcor.networkaclmanager.repo.NetworkAclRepository;
import com.futurewei.alcor.networkaclmanager.service.NetworkAclService;
import com.futurewei.alcor.web.entity.networkacl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkAclServiceImpl implements NetworkAclService {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkAclServiceImpl.class);

    @Autowired
    private NetworkAclRepository networkAclRepository;

    @Override
    public NetworkAclEntity createNetworkAcl(NetworkAclEntity networkAclEntity) throws Exception {
        //Generate uuid for networkAclEntity
        if (networkAclEntity.getId() == null) {
            networkAclEntity.setId(UUID.randomUUID().toString());
        }

        networkAclRepository.addNetworkAcl(networkAclEntity);

        //Send Network ACL to DPM here

        LOG.info("Create Network ACL success, networkAclEntity: {}", networkAclEntity);

        return networkAclRepository.getNetworkAcl(networkAclEntity.getId());
    }

    @Override
    public NetworkAclEntity updateNetworkAcl(String networkAclId, NetworkAclEntity networkAclEntity) throws Exception {
        NetworkAclEntity oldNetworkAclEntity = networkAclRepository.getNetworkAcl(networkAclId);
        if (oldNetworkAclEntity == null) {
            throw new NetworkAclNotFound();
        }

        boolean needUpdate = false;

        //Update vpc id
        if (networkAclEntity.getVpcId() != null) {
            if (!networkAclEntity.getVpcId().equals(oldNetworkAclEntity.getVpcId())) {
                oldNetworkAclEntity.setVpcId(networkAclEntity.getVpcId());
                needUpdate = true;
            }
        }

        //Update name
        if (networkAclEntity.getName() != null) {
            if (!networkAclEntity.getName().equals(oldNetworkAclEntity.getName())) {
                oldNetworkAclEntity.setName(networkAclEntity.getName());
                needUpdate = true;
            }
        }

        //Update description
        if (networkAclEntity.getDescription() != null) {
            if (!networkAclEntity.getDescription().equals(oldNetworkAclEntity.getDescription())) {
                oldNetworkAclEntity.setDescription(networkAclEntity.getDescription());
                needUpdate = true;
            }
        }

        //Update associated subnet list
        if (networkAclEntity.getAssociatedSubnets() != null) {
            List<String> newSubnets = networkAclEntity.getAssociatedSubnets();
            List<String> oldSubnets = oldNetworkAclEntity.getAssociatedSubnets();

            if (!newSubnets.equals(oldSubnets)) {
                oldNetworkAclEntity.setAssociatedSubnets(newSubnets);
                needUpdate = true;
            }
        }

        if (needUpdate) {
            networkAclRepository.addNetworkAcl(oldNetworkAclEntity);
        }

        //Send Network ACL to DPM here

        LOG.info("Update Network ACL success, networkAclEntity: {}", networkAclEntity);

        return networkAclRepository.getNetworkAcl(networkAclId);
    }

    @Override
    public void deleteNetworkAcl(String networkAclId) throws Exception {
        NetworkAclEntity networkAclEntity = networkAclRepository.getNetworkAcl(networkAclId);
        if (networkAclEntity == null) {
            throw new NetworkAclNotFound();
        }

        networkAclRepository.deleteNetworkAcl(networkAclId);

        LOG.info("Delete Network ACL success, networkAclId: {}", networkAclId);
    }

    @Override
    public NetworkAclEntity getNetworkAcl(String networkAclId) throws Exception {
        NetworkAclEntity networkAclEntity = networkAclRepository.getNetworkAcl(networkAclId);
        if (networkAclEntity == null) {
            throw new NetworkAclNotFound();
        }

        LOG.info("Get Network ACL success, networkAclEntity: {}", networkAclEntity);

        return networkAclEntity;
    }

    @Override
    public List<NetworkAclEntity> listNetworkAcl() throws Exception {
        List<NetworkAclEntity> networkAclEntities = new ArrayList<>();

        Map<String, NetworkAclEntity> networkAclEntityMap = networkAclRepository.getAllNetworkAcls();
        if (networkAclEntityMap == null) {
            return networkAclEntities;
        }

        for (Map.Entry<String, NetworkAclEntity> entry: networkAclEntityMap.entrySet()) {
            networkAclEntities.add(entry.getValue());
        }

        LOG.info("List Network ACL success");

        return networkAclEntities;
    }
}
