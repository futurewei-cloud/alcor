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

import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.networkaclmanager.exception.NetworkAclNotFound;
import com.futurewei.alcor.networkaclmanager.exception.VerifySubnetIdFailed;
import com.futurewei.alcor.networkaclmanager.exception.VerifyVpcIdFailed;
import com.futurewei.alcor.networkaclmanager.repo.NetworkAclRepository;
import com.futurewei.alcor.networkaclmanager.service.NetworkAclService;
import com.futurewei.alcor.web.entity.networkacl.*;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.restclient.SubnetManagerRestClient;
import com.futurewei.alcor.web.restclient.VpcManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ComponentScan(value="com.futurewei.alcor.web.restclient")
public class NetworkAclServiceImpl implements NetworkAclService {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkAclServiceImpl.class);

    @Autowired
    private NetworkAclRepository networkAclRepository;

    @Autowired
    private VpcManagerRestClient vpcManagerRestClient;

    @Autowired
    private SubnetManagerRestClient subnetManagerRestClient;

    private VpcEntity verifyVpcId(Object arg1, Object arg2) throws Exception {
        String projectId = (String)arg1;
        String vpcId = (String)arg2;

        //Get VpcWebJson by vpc id
        VpcWebJson vpcWebJson = vpcManagerRestClient.getVpc(projectId, vpcId);
        if (vpcWebJson == null) {
            throw new VerifyVpcIdFailed();
        }

        return vpcWebJson.getNetwork();
    }

    private SubnetEntity verifySubnetId(Object arg1, Object arg2) throws Exception {
        String projectId = (String)arg1;
        String subnetId = (String)arg2;

        //Get VpcWebJson by vpc id
        SubnetWebJson subnetWebJson = subnetManagerRestClient.getSubnet(projectId, subnetId);
        if (subnetWebJson == null) {
            throw new VerifySubnetIdFailed();
        }

        return subnetWebJson.getSubnet();
    }

    private void checkNetworkAclAsync(String projectId, NetworkAclEntity networkAclEntity,
                                      AsyncExecutor asyncExecutor) throws Exception {
        //Generate uuid for networkAclEntity
        if (networkAclEntity.getId() == null) {
            networkAclEntity.setId(UUID.randomUUID().toString());
        }

        //Verify vpc id
        asyncExecutor.runAsync(this::verifyVpcId, projectId, networkAclEntity.getVpcId());

        //Verify subnet id list
        List<String> subnetIds = networkAclEntity.getAssociatedSubnets();
        if (subnetIds != null) {
            for (String subnetId: subnetIds) {
                asyncExecutor.runAsync(this::verifySubnetId, projectId, subnetId);
            }
        }
    }

    @Override
    public NetworkAclEntity createNetworkAcl(String projectId, NetworkAclEntity networkAclEntity) throws Exception {
        AsyncExecutor asyncExecutor = new AsyncExecutor();

        checkNetworkAclAsync(projectId, networkAclEntity, asyncExecutor);

        asyncExecutor.joinAll();

        networkAclRepository.addNetworkAcl(networkAclEntity);
        List<NetworkAclRuleEntity> defaultNetworkAclRules = networkAclRepository.getDefaultNetworkAclRules();

        //Send Network ACL to DPM here

        LOG.info("Create Network ACL success, networkAclEntity: {}", networkAclEntity);
        networkAclEntity.setNetworkAclRuleEntities(defaultNetworkAclRules);
        return networkAclEntity;
    }

    @Override
    public List<NetworkAclEntity> createNetworkAclBulk(String projectId, List<NetworkAclEntity> networkAclEntities) throws Exception {
        AsyncExecutor asyncExecutor = new AsyncExecutor();
        for (NetworkAclEntity networkAclEntity: networkAclEntities) {
            checkNetworkAclAsync(projectId, networkAclEntity, asyncExecutor);
        }

        asyncExecutor.joinAll();

        networkAclRepository.addNetworkAclBulk(networkAclEntities);
        List<NetworkAclRuleEntity> defaultNetworkAclRules = networkAclRepository.getDefaultNetworkAclRules();

        for (NetworkAclEntity networkAclEntity: networkAclEntities) {
            networkAclEntity.setNetworkAclRuleEntities(defaultNetworkAclRules);
        }

        return networkAclEntities;
    }

    private boolean updateNetworkAclAsync(String projectId, NetworkAclEntity oldNetworkAclEntity,
                                          NetworkAclEntity networkAclEntity,
                                          AsyncExecutor asyncExecutor) throws Exception {
        boolean needUpdate = false;

        //Update vpc id
        if (networkAclEntity.getVpcId() != null) {
            if (!networkAclEntity.getVpcId().equals(oldNetworkAclEntity.getVpcId())) {
                asyncExecutor.runAsync(this::verifyVpcId, projectId, networkAclEntity.getVpcId());
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
            List<String> newSubnetIds = networkAclEntity.getAssociatedSubnets();
            List<String> oldSubnetIds = oldNetworkAclEntity.getAssociatedSubnets();

            if (!newSubnetIds.equals(oldSubnetIds)) {
                ArrayList<String> subnetIds = new ArrayList<>(newSubnetIds);
                subnetIds.removeAll(oldSubnetIds);
                for (String subnetId: subnetIds) {
                    asyncExecutor.runAsync(this::verifySubnetId, projectId, subnetId);
                }

                oldNetworkAclEntity.setAssociatedSubnets(newSubnetIds);
                needUpdate = true;
            }
        }

        if (needUpdate) {
            networkAclRepository.addNetworkAcl(oldNetworkAclEntity);
        }

        return needUpdate;
    }

    @Override
    public NetworkAclEntity updateNetworkAcl(String projectId, String networkAclId,
                                             NetworkAclEntity networkAclEntity) throws Exception {
        NetworkAclEntity oldNetworkAclEntity = networkAclRepository.getNetworkAcl(networkAclId);
        if (oldNetworkAclEntity == null) {
            throw new NetworkAclNotFound();
        }

        AsyncExecutor asyncExecutor = new AsyncExecutor();
        boolean needUpdate = updateNetworkAclAsync(
                projectId, oldNetworkAclEntity, networkAclEntity, asyncExecutor);
        asyncExecutor.joinAll();

        if (needUpdate) {
            networkAclRepository.addNetworkAcl(oldNetworkAclEntity);
        }

        //Send Network ACL to DPM here

        LOG.info("Update Network ACL success, networkAclEntity: {}", networkAclEntity);

        return networkAclRepository.getNetworkAcl(networkAclId);
    }

    @Override
    public List<NetworkAclEntity> updateNetworkAclBulk(String projectId, List<NetworkAclEntity> networkAclEntities) throws Exception {
        List<NetworkAclEntity> result = new ArrayList<>();

        for (NetworkAclEntity networkAclEntity: networkAclEntities) {
            result.add(updateNetworkAcl(
                    networkAclEntity.getProjectId(), networkAclEntity.getId(), networkAclEntity));
        }

        return result;
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
