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
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.proxy.IpManagerProxy;
import com.futurewei.alcor.portmanager.proxy.MacManagerProxy;
import com.futurewei.alcor.portmanager.proxy.NodeManagerProxy;
import com.futurewei.alcor.portmanager.proxy.VpcManagerProxy;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.web.entity.port.*;
import com.futurewei.alcor.web.entity.host.*;
import com.futurewei.alcor.web.entity.route.RouterState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@ComponentScan(value="com.futurewei.alcor.common.utils")
@ComponentScan(value="com.futurewei.alcor.web.restclient")
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private PortRepository portRepository;

    private void getDefaultSecurityGroup(PortEntity portEntity) {
        //FIXME: send get tenant default security group
        String defaultSgId =  "tenant-default-security-group-id";

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(defaultSgId);

        portEntity.setSecurityGroups(securityGroups);
    }

    private void verifySecurityGroup(PortEntity portEntity) {
        List<String> securityGroups = portEntity.getSecurityGroups();
        String tenantId = portEntity.getTenantId();

        for (String securityGroup: securityGroups) {
            //FIXME: send verify tenant security group
        }
    }

    private void unbindSecurityGroups(PortEntity portEntity) {

    }

    private void bindSecurityGroups(PortEntity portEntity) {
        verifySecurityGroup(portEntity);

        //FIXME: Not support yet
    }

    private void rollBackAllOperations(Stack<PortStateRollback> rollbacks) {
        while (!rollbacks.isEmpty()) {
            PortStateRollback rollback = rollbacks.pop();

            try {
                rollback.doRollback();
            } catch (Exception e) {
                LOG.error("{} roll back failed: {}", rollback, e);
            }
        }
    }

    private HostState getHostState(String hostId) {
        return null;
    }

    private void addPortToHost(String hostId) {
        HostState hostState = getHostState(hostId);

        //FIXME: Add port to Host
    }

    /**
     * Create a port, and call the interfaces of each micro-service according to the
     * configuration of the port to create various required resources for the port.
     * If any exception occurs in the added process, we need to roll back
     * the resource allocated from each micro-service.
     * @param projectId Project the port belongs to
     * @param portWebJson Port configuration
     * @return PortStateJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Override
    public PortWebJson createPortState(String projectId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Create port state, projectId: {}, PortStateJson: {}", projectId, portWebJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortEntity portEntity = portWebJson.getPortEntity();
        portEntity.setProjectId(projectId);

        try {
            //Verify VPC ID
            VpcManagerProxy vpcManagerProxy = new VpcManagerProxy(rollbacks);
            executor.runAsync(vpcManagerProxy::verifyVpc, portEntity);

            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, portEntity.getProjectId());
            if (portEntity.getFixedIps() == null) {
                executor.runAsync(ipManagerProxy::allocateRandomIpAddress, portEntity);
            } else {
                executor.runAsync(ipManagerProxy::allocateFixedIpAddress, portEntity.getFixedIps());
            }

            //Generate uuid for port
            if (portEntity.getId() == null) {
                portEntity.setId(UUID.randomUUID().toString());
            }

            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            if (portEntity.getMacAddress() == null) {
                executor.runAsync(macManagerProxy::allocateRandomMacAddress, portEntity);
            } else {
                executor.runAsync(macManagerProxy::allocateFixedMacAddress, portEntity);
            }

            //Verify security group

            //Verify Binding Host ID
            if (portEntity.getBindingHostId() != null) {
                NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(rollbacks);
                nodeManagerProxy.verifyHost(portEntity.getBindingHostId());
            }

            //Wait for all async functions to finish
            executor.joinAll();

            //Persist portState
            portRepository.addItem(portEntity);
        } catch (Exception e) {
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

        LOG.info("Create port state success, projectId: {}, PortStateJson: {}", projectId, portWebJson);

        return portWebJson;
    }

    private RouterState getRouterState(String routerId) {
        return null;
    }

    private void verifyRouter(String deviceId, String tenantId) throws Exception {
        RouterState routerState = getRouterState(deviceId);

        if (routerState == null) {
            throw new RouterNotFoundException();
        }

        if (!tenantId.equals(routerState.getTenantId())) {
            throw new RouterNotOwnedByTenant();
        }
    }

    private Map<String, Set<String>> fixedIpsToMap(List<PortEntity.FixedIp> fixedIps) {
        Map<String, Set<String>> subnetIpsMap = new HashMap<>();

        for (PortEntity.FixedIp fixedIp: fixedIps) {
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

    private List<PortEntity.FixedIp> fixedIpsCompare(List<PortEntity.FixedIp> fixedIps1, List<PortEntity.FixedIp> fixedIps2) {
        List<PortEntity.FixedIp> addFixedIps = new ArrayList<>();
        Map<String, Set<String>> subnetIpsMap = fixedIpsToMap(fixedIps2);

        for (PortEntity.FixedIp fixedIp: fixedIps1) {
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

    private void updateSecurityGroup(PortEntity portEntity, PortEntity oldPortEntity) throws Exception {
        String deviceOwner = portEntity.getDeviceOwner();

        //Network device interface does not need security groups
        if (deviceOwner != null && deviceOwner.indexOf("network") > 0) {
            throw new UpdateSecurityGroupException();
        }

        //Verify request security groups valid
        verifySecurityGroup(portEntity);

        //Delete old security groups binding
        unbindSecurityGroups(oldPortEntity);

        //Create security groups binding for port
        bindSecurityGroups(portEntity);

        oldPortEntity.setSecurityGroups(portEntity.getSecurityGroups());
    }

    private void UpdateExtraDhcpOpts(PortEntity portEntity, PortEntity portEntityOld) {

    }

    private void updatePortToHost(PortEntity portEntity) {

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
    public PortWebJson updatePortState(String projectId, String portId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Update port state, projectId: {}, portId: {}, PortStateJson: {}",
                projectId, portId, portWebJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortEntity portEntity = portWebJson.getPortEntity();
        PortEntity oldPortEntity = portRepository.findItem(portId);

        try {
            if (portRepository.findItem(portId) == null) {
                throw new PortStateNotFoundException();
            }

            portEntity.setProjectId(projectId);

            //Update mac_address

            //Update device_owner and device_id
            String deviceOwnerNew = portEntity.getDeviceOwner();
            String deviceIdNew = portEntity.getDeviceId();
            String deviceIdOld = oldPortEntity.getDeviceId();
            String tenantId = oldPortEntity.getTenantId();

            if (deviceOwnerNew != null && deviceIdNew != null && !deviceIdNew.equals(deviceIdOld)) {
                if (DeviceOwner.ROUTER.getOwner().equals(deviceOwnerNew)) {
                    verifyRouter(deviceIdNew, tenantId);
                }
            }

            //Update fixed_ips
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, projectId);

            if (fixedIps != null) {
                List<PortEntity.FixedIp> oldFixedIps = oldPortEntity.getFixedIps();

                List<PortEntity.FixedIp> addFixedIps = fixedIpsCompare(fixedIps, oldFixedIps);
                List<PortEntity.FixedIp> delFixedIps = fixedIpsCompare(oldFixedIps, fixedIps);

                if (delFixedIps.size() > 0) {
                    executor.runAsync(ipManagerProxy::releaseIpAddressBulk, delFixedIps);
                }

                if (addFixedIps.size() > 0) {
                    executor.runAsync(ipManagerProxy::allocateFixedIpAddress, addFixedIps);
                }
            } else {
                List<PortEntity.FixedIp> oldFixedIps = oldPortEntity.getFixedIps();
                executor.runAsync(ipManagerProxy::releaseIpAddressBulk, oldFixedIps);
            }

            oldPortEntity.setFixedIps(fixedIps);

            //Update security_groups
            updateSecurityGroup(oldPortEntity, portEntity);

            //Update allow_address_pairs
            //UpdateAllowAddressPairs();

            //Update extra_dhcp_opts
            UpdateExtraDhcpOpts(portEntity, oldPortEntity);

            //Update binding:host_id
            if (portEntity.getBindingHostId() != null) {
                NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(rollbacks);
                nodeManagerProxy.verifyHost(portEntity.getBindingHostId());
            }

            oldPortEntity.setBindingHostId(portEntity.getBindingHostId());

            //Wait for all async functions to finish
            executor.joinAll();

            portRepository.addItem(oldPortEntity);
            portWebJson.setPortEntity(oldPortEntity);
        } catch (Exception e) {
            executor.waitAll();
            rollBackAllOperations(rollbacks);
            throw e;
        }

        LOG.debug("Update port state success, portStateJson: {}", portWebJson);

        return portWebJson;
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
    public void deletePortState(String projectId, String portId) throws Exception {
        LOG.debug("Delete port state, projectId: {}, portId: {}", projectId, portId);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortEntity portEntity = portRepository.findItem(portId);
        if (portEntity == null) {
            throw new PortStateNotFoundException();
        }

        try {
            //Release ip address
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, projectId);
            if (portEntity.getFixedIps() != null && portEntity.getFixedIps().size() > 0) {
                executor.runAsync(ipManagerProxy::releaseIpAddressBulk, portEntity.getFixedIps());
            }

            //Release mac address
            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            if (portEntity.getMacAddress() != null && !"".equals(portEntity.getMacAddress())) {
                executor.runAsync(macManagerProxy::releaseMacAddress, portEntity);
            }

            //Unbind security groups
            unbindSecurityGroups(portEntity);

            //Wait for all async functions to finish
            executor.joinAll();

            portRepository.deleteItem(portId);
        } catch (Exception e) {
            executor.waitAll();
            rollBackAllOperations(rollbacks);
            throw e;
        }

        LOG.debug("Delete port state success, projectId: {}, portId: {}", projectId, portId);
    }

    /**
     * Get the configuration of the port by port id
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @return PortStateJson
     * @throws Exception Db operation exception
     */
    @Override
    public PortWebJson getPortState(String projectId, String portId) throws Exception {
        PortEntity portEntity = portRepository.findItem(portId);
        if (portEntity == null) {
            throw new PortStateNotFoundException();
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
    public List<PortWebJson> listPortState(String projectId) throws Exception {
        List<PortWebJson> result = new ArrayList<>();

        Map<String, PortEntity> portStateMap = portRepository.findAllItems();
        if (portStateMap == null) {
            return result;
        }

        for (Map.Entry<String, PortEntity> entry: portStateMap.entrySet()) {
            PortWebJson portWebJson = new PortWebJson(entry.getValue());
            result.add(portWebJson);
        }

        return result;
    }
}
