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
package com.futurewei.alcor.portmanager.service.implement;

import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.portmanager.dao.PortDao;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.portmanager.proxy.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.portmanager.util.GoalStateUtil;
import com.futurewei.alcor.schema.Goalstate.*;
import com.futurewei.alcor.schema.Common.*;
import com.futurewei.alcor.web.entity.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@ComponentScan(value="com.futurewei.alcor.common.utils")
@ComponentScan(value="com.futurewei.alcor.web.restclient")
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private PortDao portDao;

    private void rollBackAllOperations(Stack<Rollback> rollbacks) {
        while (!rollbacks.isEmpty()) {
            Rollback rollback = rollbacks.pop();

            try {
                rollback.doRollback();
            } catch (Exception e) {
                LOG.error("{} roll back failed: {}", rollback, e);
            }
        }
    }

    private void createPortAsync(PortEntity portEntity, AsyncExecutor executor, Stack<Rollback> rollbacks) throws Exception {
        //Verify VPC ID
        VpcManagerProxy vpcManagerProxy = new VpcManagerProxy(rollbacks);
        executor.runAsync(vpcManagerProxy::getVpcEntity, portEntity);

        //Allocate IP address
        IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, portEntity.getProjectId());
        SubnetManagerProxy subnetManagerProxy = new SubnetManagerProxy(portEntity.getProjectId());
        if (portEntity.getFixedIps() != null) {
            for (FixedIp fixedIp: portEntity.getFixedIps()) {
                executor.runAsyncThenAccept(subnetManagerProxy::getSubnetEntity,
                        ipManagerProxy::allocateFixedIpAddress,
                        fixedIp, fixedIp);
            }
        } else {
            executor.runAsyncThenApply(ipManagerProxy::allocateRandomIpAddress,
                    subnetManagerProxy::getSubnetEntity,
                    portEntity);
        }

        //Generate uuid for port
        if (portEntity.getId() == null) {
            portEntity.setId(UUID.randomUUID().toString());
        }

        //Allocate MAC address
        MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
        if (portEntity.getMacAddress() == null) {
            executor.runAsync(macManagerProxy::allocateRandomMacAddress, portEntity);
        } else {
            executor.runAsync(macManagerProxy::allocateFixedMacAddress, portEntity);
        }

        //Verify and bind security group
        SecurityGroupManagerProxy securityGroupManagerProxy = new SecurityGroupManagerProxy(portEntity.getProjectId());
        if (portEntity.getSecurityGroups() != null) {
            for (String securityGroupId: portEntity.getSecurityGroups()) {
                executor.runAsync(securityGroupManagerProxy::getSecurityGroup, securityGroupId);
                executor.runAsync(securityGroupManagerProxy::bindSecurityGroup, portEntity);
            }
        } else {
            //Do we need to bind default security group? No, we don't
            executor.runAsync(securityGroupManagerProxy::getDefaultSecurityGroupEntity, portEntity.getTenantId());
        }

        //Get subnet route
        if (portEntity.getFixedIps() != null) {
            RouteManagerProxy routeManagerProxy = new RouteManagerProxy(rollbacks);

            for (FixedIp fixedIp: portEntity.getFixedIps()) {
                executor.runAsync(routeManagerProxy::getRouteBySubnetId, fixedIp.getSubnetId());
            }
        }

        //Verify Binding Host ID
        if (portEntity.getBindingHostId() != null) {
            NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(rollbacks);
            executor.runAsync(nodeManagerProxy::getNodeInfo, portEntity.getBindingHostId());
        }
    }

    private void exceptionHandle(AsyncExecutor executor, Stack<Rollback> rollbacks, Exception e) throws Exception {
        /**
         When an exception occurs, we need to roll back all asynchronous operations,
         and some asynchronous may not be finished yet.if we roll back at this time,
         they may not be completed until the rollback operation is completed.
         as a result, they cannot be rolled back.
         */
        executor.waitAll();
        rollBackAllOperations(rollbacks);
        throw e;
    }

    /**
     * Create a port, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for the port.
     * If any exception occurs in the added process, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebJson Port configuration
     * @return PortWebJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Override
    public PortWebJson createPort(String projectId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Create port, projectId: {}, PortWebJson: {}", projectId, portWebJson);

        Stack<Rollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortEntity portEntity = portWebJson.getPortEntity();
        portEntity.setProjectId(projectId);

        try {
            createPortAsync(portEntity, executor, rollbacks);

            //Wait for all async functions to finish
            List<Object> entities = executor.joinAll();
            entities.add(portEntity);

            //Build GoalState and Send it to DPM
            if (portEntity.getBindingHostId() != null) {
                GoalState goalState = GoalStateUtil.buildGoalState(entities, OperationType.CREATE);
                DataPlaneManagerProxy dataPlaneManagerProxy = new DataPlaneManagerProxy(rollbacks);
                dataPlaneManagerProxy.createGoalState(goalState);
            }

            //Persist portEntity
            //portRepository.addItem(portEntity);
            portDao.createPort(portEntity);
        } catch (Exception e) {
            exceptionHandle(executor, rollbacks, e);
        }

        LOG.info("Create port success, projectId: {}, portWebJson: {}", projectId, portWebJson);

        return portWebJson;
    }

    /**
     * Create multiple ports, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for all ports.
     * If an exception occurs during the creation of multiple ports, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebBulkJson Multiple ports configuration
     * @return PortWebBulkJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Override
    public PortWebBulkJson createPortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        Stack<Rollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        try {
            for (PortEntity portEntity: portWebBulkJson.getPortEntities()) {
                portEntity.setProjectId(projectId);
                createPortAsync(portEntity, executor, rollbacks);
            }

            //Wait for all async functions to finish
            List<Object> entities = executor.joinAll();
            for (PortEntity portEntity: portWebBulkJson.getPortEntities()) {
                if (portEntity.getBindingHostId() != null) {
                    entities.add(portEntity);
                }
            }

            //Build GoalState and Send it to DPM
            DataPlaneManagerProxy dataPlaneManagerProxy = new DataPlaneManagerProxy(rollbacks);
            GoalState goalState = GoalStateUtil.buildGoalState(entities, OperationType.CREATE);
            if (goalState.getPortStatesCount() > 0) {
                dataPlaneManagerProxy.createGoalState(goalState);
            }

            //Persist portEntities
            //portRepository.addItems(portWebBulkJson.getPortEntities());
            portDao.createPorts(portWebBulkJson.getPortEntities());
        } catch (Exception e) {
            exceptionHandle(executor, rollbacks, e);
        }

        return portWebBulkJson;
    }

    private Map<String, Set<String>> fixedIpsToMap(List<FixedIp> fixedIps) {
        Map<String, Set<String>> subnetIpsMap = new HashMap<>();

        for (FixedIp fixedIp: fixedIps) {
            if (subnetIpsMap.containsKey(fixedIp.getSubnetId())) {
                subnetIpsMap.get(fixedIp.getSubnetId()).add(fixedIp.getIpAddress());
            } else {
                Set<String> ips = new HashSet<>();
                ips.add(fixedIp.getIpAddress());
                subnetIpsMap.put(fixedIp.getSubnetId(), ips);
            }
        }

        return subnetIpsMap;
    }

    private List<FixedIp> fixedIpsCompare(List<FixedIp> fixedIps1, List<FixedIp> fixedIps2) {
        List<FixedIp> addFixedIps = new ArrayList<>();
        Map<String, Set<String>> subnetIpsMap = fixedIpsToMap(fixedIps2);

        for (FixedIp fixedIp: fixedIps1) {
            String subnetId = fixedIp.getSubnetId();
            String ipAddress = fixedIp.getIpAddress();
            if (subnetIpsMap.containsKey(subnetId)) {
                if (!subnetIpsMap.get(subnetId).contains(ipAddress)) {
                    addFixedIps.add(fixedIp);
                }
            } else {
                addFixedIps.add(fixedIp);
            }
        }

        return addFixedIps;
    }

    private boolean updatePortAsync(PortEntity newPortEntity, PortEntity oldPortEntity, AsyncExecutor executor,
                                 Stack<Rollback> rollbacks) throws Exception {
        boolean needNotifyDpm = false;

        //Update name
        String newName = newPortEntity.getName();
        String oldName = oldPortEntity.getName();
        if (newName != null && !newName.equals(oldName)) {
            oldPortEntity.setName(newName);
        }

        //Update admin_state
        boolean newAdminState = newPortEntity.isAdminStateUp();
        boolean oldAdminState = oldPortEntity.isAdminStateUp();
        if (newAdminState != oldAdminState) {
            oldPortEntity.setAdminStateUp(newAdminState);
            needNotifyDpm = true;
        }

        //Update binding:host_id
        String newBindingHostId = newPortEntity.getBindingHostId();
        String oldBindingHostId = oldPortEntity.getBindingHostId();
        if (newBindingHostId != null && !newBindingHostId.equals(oldBindingHostId)) {
            oldPortEntity.setBindingHostId(newBindingHostId);
            needNotifyDpm = true;
        }

        //Update binding:profile
        String newBindingProfile = newPortEntity.getBindingProfile();
        String oldBindingProfile = oldPortEntity.getBindingProfile();
        if (newBindingProfile != null && !newBindingProfile.equals(oldBindingProfile)) {
            oldPortEntity.setBindingProfile(newBindingProfile);
            needNotifyDpm = true;
        }

        //Update binding:vnic_type
        String newBindingVnicType = newPortEntity.getBindingVnicType();
        String oldBindingVnicType = oldPortEntity.getBindingVnicType();
        if (newBindingVnicType != null && !newBindingVnicType.equals(oldBindingVnicType)) {
            oldPortEntity.setBindingVnicType(newBindingVnicType);
            needNotifyDpm = true;
        }

        //Update description
        String newDescription = newPortEntity.getDescription();
        String oldDescription = oldPortEntity.getDescription();
        if (newDescription != null && !newDescription.equals(oldDescription)) {
            oldPortEntity.setDescription(newDescription);
        }

        //Update device_id
        String newDeviceId = newPortEntity.getDeviceId();
        String oldDeviceId = oldPortEntity.getDeviceId();
        if (newDeviceId != null && !newDeviceId.equals(oldDeviceId)) {
            oldPortEntity.setDeviceId(newDeviceId);
            needNotifyDpm = true;
        }

        //Update device_owner
        String newDeviceOwner = newPortEntity.getDeviceOwner();
        String oldDeviceOwner = oldPortEntity.getDeviceOwner();
        if (newDeviceOwner != null && !newDeviceOwner.equals(oldDeviceOwner)) {
            oldPortEntity.setDeviceOwner(newDeviceOwner);
        }

        //Update dns_domain
        String newDnsDomain = newPortEntity.getDnsDomain();
        String oldDnsDomain = oldPortEntity.getDnsDomain();
        if (newDnsDomain != null && !newDnsDomain.equals(oldDnsDomain)) {
            oldPortEntity.setDnsDomain(newDnsDomain);
            needNotifyDpm = true;
        }

        //Update dns_name
        String newDnsName = newPortEntity.getDnsName();
        String oldDnsName = oldPortEntity.getDnsName();
        if (newDnsName != null && !newDnsName.equals(oldDnsName)) {
            oldPortEntity.setDnsName(newDnsName);
            needNotifyDpm = true;
        }

        //Update extra_dhcp_opts
        List<ExtraDhcpOpt> newExtraDhcpOpts = newPortEntity.getExtraDhcpOpts();
        List<ExtraDhcpOpt> oldExtraDhcpOpts = oldPortEntity.getExtraDhcpOpts();
        if (newExtraDhcpOpts != null && !newExtraDhcpOpts.equals(oldExtraDhcpOpts)) {
            oldPortEntity.setExtraDhcpOpts(newExtraDhcpOpts);
            needNotifyDpm = true;
        }

        //Update fixed_ips
        List<FixedIp> newFixedIps = newPortEntity.getFixedIps();
        List<FixedIp> oldFixedIps = oldPortEntity.getFixedIps();
        if (newFixedIps != null && !newFixedIps.equals(oldFixedIps)) {
            List<FixedIp> addFixedIps = fixedIpsCompare(newFixedIps, oldFixedIps);
            List<FixedIp> delFixedIps = fixedIpsCompare(oldFixedIps, newFixedIps);
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, newPortEntity.getProjectId());

            if (delFixedIps.size() > 0) {
                needNotifyDpm = true;
                executor.runAsync(ipManagerProxy::releaseIpAddressBulk, delFixedIps);
            }

            if (addFixedIps.size() > 0) {
                needNotifyDpm = true;
                executor.runAsync(ipManagerProxy::allocateFixedIpAddresses, addFixedIps);
            }

            oldPortEntity.setFixedIps(newFixedIps);
        }

        //Update mac_address
        String macAddress = newPortEntity.getMacAddress();
        String oldMacAddress = oldPortEntity.getMacAddress();
        if (macAddress != null && !oldMacAddress.equals(macAddress)) {
            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            executor.runAsync(macManagerProxy::updateMacAddress, oldPortEntity, newPortEntity);
            oldPortEntity.setMacAddress(macAddress);
            needNotifyDpm = true;
        }

        //Update allow_address_pairs
        List<AllowAddressPair> newAllowedAddressPairs = newPortEntity.getAllowedAddressPairs();
        List<AllowAddressPair> oldAllowedAddressPairs = oldPortEntity.getAllowedAddressPairs();
        if (newAllowedAddressPairs != null && !newAllowedAddressPairs.equals(oldAllowedAddressPairs)) {
            oldPortEntity.setAllowedAddressPairs(newAllowedAddressPairs);
            needNotifyDpm = true;
        }

        //Update port_security_enabled
        boolean newPortSecurityEnabled = newPortEntity.isPortSecurityEnabled();
        boolean oldPortSecurityEnabled = oldPortEntity.isPortSecurityEnabled();
        if (newPortSecurityEnabled != oldPortSecurityEnabled) {
            oldPortEntity.setPortSecurityEnabled(newPortSecurityEnabled);
            needNotifyDpm = true;
        }

        //Update qos_policy_id
        String newQosPolicyId = newPortEntity.getQosPolicyId();
        String oldQosPolicyId = oldPortEntity.getQosPolicyId();
        if (newQosPolicyId!= null && !newQosPolicyId.equals(oldQosPolicyId)) {
            oldPortEntity.setQosPolicyId(newQosPolicyId);
            needNotifyDpm = true;
        }

        //Update security_groups
        List<String> newSecurityGroups = newPortEntity.getSecurityGroups();
        List<String> oldSecurityGroups = oldPortEntity.getSecurityGroups();
        if (newSecurityGroups != null && !newSecurityGroups.equals(oldSecurityGroups)) {
            //FIXME: update security group
            SecurityGroupManagerProxy securityGroupManagerProxy = new SecurityGroupManagerProxy(newPortEntity.getProjectId());
            executor.runAsync(securityGroupManagerProxy::unbindSecurityGroup, oldPortEntity);
            executor.runAsync(securityGroupManagerProxy::bindSecurityGroup, newPortEntity);
            oldPortEntity.setSecurityGroups(newSecurityGroups);
            needNotifyDpm = true;
        }

        //Update mac_learning_enabled
        boolean newMacLearningEnabled = newPortEntity.isMacLearningEnabled();
        boolean oldMacLearningEnabled = oldPortEntity.isMacLearningEnabled();
        if (newMacLearningEnabled != oldMacLearningEnabled) {
            oldPortEntity.setMacLearningEnabled(newMacLearningEnabled);
            needNotifyDpm = true;
        }

        return needNotifyDpm;
    }

    private void getPortDependentResources(PortEntity portEntity, AsyncExecutor executor) {
        //Get VpcEntity
        VpcManagerProxy vpcManagerProxy = new VpcManagerProxy(null);
        executor.runAsync(vpcManagerProxy::getVpcEntity, portEntity);

        //Get SubnetEntity and subnet route
        SubnetManagerProxy subnetManagerProxy = new SubnetManagerProxy(portEntity.getProjectId());
        RouteManagerProxy routeManagerProxy = new RouteManagerProxy(null);
        for (FixedIp fixedIp: portEntity.getFixedIps()) {
            executor.runAsync(subnetManagerProxy::getSubnetEntity, fixedIp);
            executor.runAsync(routeManagerProxy::getRouteBySubnetId, fixedIp.getSubnetId());
        }

        //Get SecurityGroupEntity
        SecurityGroupManagerProxy securityGroupManagerProxy = new SecurityGroupManagerProxy(portEntity.getProjectId());
        if (portEntity.getSecurityGroups() != null) {
            for (String securityGroupId: portEntity.getSecurityGroups()) {
                executor.runAsync(securityGroupManagerProxy::getSecurityGroup, securityGroupId);
            }
        } else {
            executor.runAsync(securityGroupManagerProxy::getDefaultSecurityGroupEntity, portEntity.getTenantId());
        }

        //Get NodeInfo
        NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(null);
        if (portEntity.getBindingHostId() != null) {
            executor.runAsync(nodeManagerProxy::getNodeInfo, portEntity.getBindingHostId());
        }

        //Get Neighbors
    }

    /**
     * Update the configuration information of port. Resources requested from various
     * micro-services may need to be updated according to the new configuration of port.
     * If any exception occurs in the updated process, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @param portWebJson The new configuration of port
     * @return The new configuration of port
     * @throws Exception Various exceptions that may occur during the update process
     */
    @Override
    public PortWebJson updatePort(String projectId, String portId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Update port, projectId: {}, portId: {}, PortWebJson: {}",
                projectId, portId, portWebJson);

        Stack<Rollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortEntity portEntity = portWebJson.getPortEntity();
        portEntity.setProjectId(projectId);

        try {
            //PortEntity oldPortEntity = portRepository.findItem(portId);
            PortEntity oldPortEntity = portDao.findPort(portId);
            if (oldPortEntity == null) {
                throw new PortEntityNotFound();
            }

            boolean needNotifyDpm = updatePortAsync(portEntity, oldPortEntity, executor, rollbacks);
            if (needNotifyDpm) {
                getPortDependentResources(oldPortEntity, executor);
            }

            //Wait for all async functions to finish
            List<Object> entities = executor.joinAll();

            //Build GoalState and send it to DPM
            if (needNotifyDpm) {
                entities.add(oldPortEntity);
                DataPlaneManagerProxy dataPlaneManagerProxy = new DataPlaneManagerProxy(rollbacks);
                GoalState goalState = GoalStateUtil.buildGoalState(entities, OperationType.UPDATE);
                dataPlaneManagerProxy.updateGoalState(goalState);
            }

            //Persist the new configuration of port to the db
            //portRepository.addItem(oldPortEntity);
            portDao.updatePort(oldPortEntity);
            portWebJson.setPortEntity(oldPortEntity);
        } catch (Exception e) {
            exceptionHandle(executor, rollbacks, e);
        }

        LOG.debug("Update port success, portWebJson: {}", portWebJson);

        return portWebJson;
    }

    /**
     * Update the configuration information of ports. Resources requested from various
     * micro-services may need to be updated according to the new configuration of ports.
     * If an exception occurs during the update, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebBulkJson The new configuration of ports
     * @return The new configuration of ports
     * @throws Exception Various exceptions that may occur during the update process
     */
    @Override
    public PortWebBulkJson updatePortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        Stack<Rollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();
        List<PortEntity> portEntities = new ArrayList<>();

        try {
            for (PortEntity portEntity: portWebBulkJson.getPortEntities()) {
                portEntity.setProjectId(projectId);
                //PortEntity oldPortEntity = portRepository.findItem(portEntity.getId());
                PortEntity oldPortEntity = portDao.findPort(portEntity.getId());
                if (oldPortEntity == null) {
                    throw new PortEntityNotFound();
                }

                boolean needNotifyDpm = updatePortAsync(portEntity, oldPortEntity, executor, rollbacks);
                if (needNotifyDpm) {
                    portEntities.add(oldPortEntity);
                    getPortDependentResources(oldPortEntity, executor);
                }
            }

            //Wait for all async functions to finish
            List<Object> entities = executor.joinAll();
            entities.addAll(portEntities);

            //Build GoalState and send it to DPM
            DataPlaneManagerProxy dataPlaneManagerProxy = new DataPlaneManagerProxy(rollbacks);
            GoalState goalState = GoalStateUtil.buildGoalState(entities, OperationType.UPDATE);
            if (goalState.getPortStatesCount() > 0) {
                dataPlaneManagerProxy.updateGoalState(goalState);
            }

            //Persist portEntities
            //portRepository.addItems(portEntities);
            portDao.createPorts(portEntities);
            portWebBulkJson.setPortEntities(portEntities);
        } catch (Exception e) {
            exceptionHandle(executor, rollbacks, e);
        }

        return portWebBulkJson;
    }

    /**
     * Delete the port corresponding to portId from the repository and delete
     * the resources requested by the port from each micro-service.
     * If any exception occurs in the deleted process, we need to roll back
     * the resource deletion operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @throws Exception Various exceptions that may occur during the delete process
     */
    @Override
    public void deletePort(String projectId, String portId) throws Exception {
        LOG.debug("Delete port, projectId: {}, portId: {}", projectId, portId);

        Stack<Rollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        //PortEntity portEntity = portRepository.findItem(portId);
        PortEntity portEntity = portDao.findPort(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        try {
            //Release ip address
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, projectId);
            executor.runAsync(ipManagerProxy::releaseIpAddressBulk, portEntity.getFixedIps());

            //Release mac address
            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            executor.runAsync(macManagerProxy::releaseMacAddress, portEntity);

            //Unbind security groups
            if (portEntity.getSecurityGroups() != null) {
                SecurityGroupManagerProxy securityGroupManagerProxy = new SecurityGroupManagerProxy(portEntity.getProjectId());
                executor.runAsync(securityGroupManagerProxy::unbindSecurityGroup, portEntity);
            }

            //Get port dependent resources
            getPortDependentResources(portEntity, executor);

            //Wait for all async functions to finish
            List<Object> entities = executor.joinAll();
            entities.add(portEntity);

            //Build GoalState and send it to DPM
            DataPlaneManagerProxy dataPlaneManagerProxy = new DataPlaneManagerProxy(rollbacks);
            GoalState goalState = GoalStateUtil.buildGoalState(entities, OperationType.DELETE);
            dataPlaneManagerProxy.deleteGoalState(goalState);

            //portRepository.deleteItem(portId);
            portDao.deletePort(portId);
        } catch (Exception e) {
            exceptionHandle(executor, rollbacks, e);
        }

        LOG.debug("Delete port success, projectId: {}, portId: {}", projectId, portId);
    }

    /**
     * Get the configuration of the port by port id
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @return PortWebJson
     * @throws Exception Db operation exception
     */
    @Override
    public PortWebJson getPort(String projectId, String portId) throws Exception {
        //PortEntity portEntity = portRepository.findItem(portId);
        PortEntity portEntity = portDao.findPort(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        return new PortWebJson(portEntity);
    }

    /**
     * Get all port information
     * @param projectId Project the port belongs to
     * @return A list of port information
     * @throws Exception Db operation exception
     */
    @Override
    public List<PortWebJson> listPort(String projectId) throws Exception {
        List<PortWebJson> result = new ArrayList<>();

        //Map<String, PortEntity> portEntityMap = portRepository.findAllItems();
        List<PortEntity> portEntities = portDao.listPort();
        if (portEntities == null) {
            return result;
        }

        for (PortEntity portEntity: portEntities) {
            PortWebJson portWebJson = new PortWebJson(portEntity);
            result.add(portWebJson);
        }

        return result;
    }
}
