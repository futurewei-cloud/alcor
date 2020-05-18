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
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private PortRepository portRepository;

    private void getDefaultSecurityGroup(PortState portState) {
        //FIXME: send get tenant default security group
        String defaultSgId =  "tenant-default-security-group-id";

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(defaultSgId);

        portState.setSecurityGroups(securityGroups);
    }

    private void verifySecurityGroup(PortState portState) {
        List<String> securityGroups = portState.getSecurityGroups();
        String tenantId = portState.getTenantId();

        for (String securityGroup: securityGroups) {
            //FIXME: send verify tenant security group
        }
    }

    private void unbindSecurityGroups(PortState portState) {

    }

    private void bindSecurityGroups(PortState portState) {
        verifySecurityGroup(portState);

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
     * @param portStateJson Port configuration
     * @return PortStateJson
     * @throws Exception Various exceptions that may occur during the create process
     */
    @Override
    public PortStateJson createPortState(String projectId, PortStateJson portStateJson) throws Exception {
        LOG.debug("Create port state, projectId: {}, PortStateJson: {}", projectId, portStateJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortState portState = portStateJson.getPortState();
        portState.setProjectId(projectId);

        try {
            //Verify VPC ID
            VpcManagerProxy vpcManagerProxy = new VpcManagerProxy(rollbacks);
            executor.runAsync(vpcManagerProxy::verifyVpc, portState);

            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, portState.getProjectId());
            if (portState.getFixedIps() == null) {
                executor.runAsync(ipManagerProxy::allocateRandomIpAddress, portState);
            } else {
                executor.runAsync(ipManagerProxy::allocateFixedIpAddress, portState.getFixedIps());
            }

            //Generate uuid for port
            if (portState.getId() == null) {
                portState.setId(UUID.randomUUID().toString());
            }

            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            if (portState.getMacAddress() == null) {
                executor.runAsync(macManagerProxy::allocateRandomMacAddress, portState);
            } else {
                executor.runAsync(macManagerProxy::allocateFixedMacAddress, portState);
            }

            //Verify security group

            //Verify Binding Host ID
            if (portState.getBindingHostId() != null) {
                NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(rollbacks);
                nodeManagerProxy.verifyHost(portState.getBindingHostId());
            }

            //Wait for all async functions to finish
            executor.joinAll();

            //Persist portState
            portRepository.addItem(portState);
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

        LOG.info("Create port state success, projectId: {}, PortStateJson: {}", projectId, portStateJson);

        return portStateJson;
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

    private Map<String, Set<String>> fixedIpsToMap(List<PortState.FixedIp> fixedIps) {
        Map<String, Set<String>> subnetIpsMap = new HashMap<>();

        for (PortState.FixedIp fixedIp: fixedIps) {
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

    private List<PortState.FixedIp> fixedIpsCompare(List<PortState.FixedIp> fixedIps1, List<PortState.FixedIp> fixedIps2) {
        List<PortState.FixedIp> addFixedIps = new ArrayList<>();
        Map<String, Set<String>> subnetIpsMap = fixedIpsToMap(fixedIps2);

        for (PortState.FixedIp fixedIp: fixedIps1) {
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

    private void updateSecurityGroup(PortState portState, PortState oldPortState) throws Exception {
        String deviceOwner = portState.getDeviceOwner();

        //Network device interface does not need security groups
        if (deviceOwner != null && deviceOwner.indexOf("network") > 0) {
            throw new UpdateSecurityGroupException();
        }

        //Verify request security groups valid
        verifySecurityGroup(portState);

        //Delete old security groups binding
        unbindSecurityGroups(oldPortState);

        //Create security groups binding for port
        bindSecurityGroups(portState);

        oldPortState.setSecurityGroups(portState.getSecurityGroups());
    }

    private void UpdateExtraDhcpOpts(PortState portState, PortState portStateOld) {

    }

    private void updatePortToHost(PortState portState) {

    }

    /**
     * Update the configuration information of port. Resources requested from various
     * micro-services may need to be updated according to the new configuration of port.
     * If any exception occurs in the updated process, we need to roll back
     * the resource added or deleted operation of each micro-service.
     * @param projectId Project the port belongs to
     * @param portId Id of port
     * @param portStateJson The new configuration of port
     * @return The new configuration of port
     * @throws Exception Various exceptions that may occur during the update process
     */
    @Override
    public PortStateJson updatePortState(String projectId, String portId, PortStateJson portStateJson) throws Exception {
        LOG.debug("Update port state, projectId: {}, portId: {}, PortStateJson: {}",
                projectId, portId, portStateJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        AsyncExecutor executor = new AsyncExecutor();

        PortState portState = portStateJson.getPortState();
        PortState oldPortState = portRepository.findItem(portId);

        try {
            if (portRepository.findItem(portId) == null) {
                throw new PortStateNotFoundException();
            }

            portState.setProjectId(projectId);

            //Update mac_address

            //Update device_owner and device_id
            String deviceOwnerNew = portState.getDeviceOwner();
            String deviceIdNew = portState.getDeviceId();
            String deviceIdOld = oldPortState.getDeviceId();
            String tenantId = oldPortState.getTenantId();

            if (deviceOwnerNew != null && deviceIdNew != null && !deviceIdNew.equals(deviceIdOld)) {
                if (DeviceOwner.ROUTER.getOwner().equals(deviceOwnerNew)) {
                    verifyRouter(deviceIdNew, tenantId);
                }
            }

            //Update fixed_ips
            List<PortState.FixedIp> fixedIps = portState.getFixedIps();
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, projectId);

            if (fixedIps != null) {
                List<PortState.FixedIp> oldFixedIps = oldPortState.getFixedIps();

                List<PortState.FixedIp> addFixedIps = fixedIpsCompare(fixedIps, oldFixedIps);
                List<PortState.FixedIp> delFixedIps = fixedIpsCompare(oldFixedIps, fixedIps);

                if (delFixedIps.size() > 0) {
                    executor.runAsync(ipManagerProxy::releaseIpAddressBulk, delFixedIps);
                }

                if (addFixedIps.size() > 0) {
                    executor.runAsync(ipManagerProxy::allocateFixedIpAddress, addFixedIps);
                }
            } else {
                List<PortState.FixedIp> oldFixedIps = oldPortState.getFixedIps();
                executor.runAsync(ipManagerProxy::releaseIpAddressBulk, oldFixedIps);
            }

            oldPortState.setFixedIps(fixedIps);

            //Update security_groups
            updateSecurityGroup(oldPortState, portState);

            //Update allow_address_pairs
            //UpdateAllowAddressPairs();

            //Update extra_dhcp_opts
            UpdateExtraDhcpOpts(portState, oldPortState);

            //Update binding:host_id
            if (portState.getBindingHostId() != null) {
                NodeManagerProxy nodeManagerProxy = new NodeManagerProxy(rollbacks);
                nodeManagerProxy.verifyHost(portState.getBindingHostId());
            }

            oldPortState.setBindingHostId(portState.getBindingHostId());

            //Wait for all async functions to finish
            executor.joinAll();

            portRepository.addItem(oldPortState);
            portStateJson.setPortState(oldPortState);
        } catch (Exception e) {
            executor.waitAll();
            rollBackAllOperations(rollbacks);
            throw e;
        }

        LOG.debug("Update port state success, portStateJson: {}", portStateJson);

        return portStateJson;
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

        PortState portState = portRepository.findItem(portId);
        if (portState == null) {
            throw new PortStateNotFoundException();
        }

        try {
            //Release ip address
            IpManagerProxy ipManagerProxy = new IpManagerProxy(rollbacks, projectId);
            if (portState.getFixedIps() != null && portState.getFixedIps().size() > 0) {
                executor.runAsync(ipManagerProxy::releaseIpAddressBulk, portState.getFixedIps());
            }

            //Release mac address
            MacManagerProxy macManagerProxy = new MacManagerProxy(rollbacks);
            if (portState.getMacAddress() != null && !"".equals(portState.getMacAddress())) {
                executor.runAsync(macManagerProxy::releaseMacAddress, portState);
            }

            //Unbind security groups
            unbindSecurityGroups(portState);

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
    public PortStateJson getPortState(String projectId, String portId) throws Exception {
        PortState portState = portRepository.findItem(portId);
        if (portState == null) {
            throw new PortStateNotFoundException();
        }

        return new PortStateJson(portState);
    }

    /**
     * Get all port information
     * @param projectId Project the port belongs to
     * @return A list of port information
     * @throws Exception Db operation exception
     */
    @Override
    public List<PortStateJson> listPortState(String projectId) throws Exception {
        List<PortStateJson> result = new ArrayList<>();

        Map<String, PortState> portStateMap = portRepository.findAllItems();
        if (portStateMap == null) {
            return result;
        }

        for (Map.Entry<String, PortState> entry: portStateMap.entrySet()) {
            PortStateJson portStateJson = new PortStateJson(entry.getValue());
            result.add(portStateJson);
        }

        return result;
    }
}
