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


import com.futurewei.alcor.portmanager.executor.AsyncExecutor;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.restwrap.IpAddressRestWrap;
import com.futurewei.alcor.portmanager.restwrap.MacAddressRestWrap;
import com.futurewei.alcor.portmanager.restwrap.VpcRestWrap;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.web.entity.*;
import com.futurewei.alcor.web.rest.IpAddressRest;
import com.futurewei.alcor.web.rest.MacAddressRest;
import com.futurewei.alcor.web.rest.VpcRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@ComponentScan(value="com.futurewei.alcor.web.rest")
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private IpAddressRest ipAddressRest;

    @Autowired
    private MacAddressRest macAddressRest;

    @Autowired
    private VpcRest vpcRest;


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

    private void rollBackAllOperations(Stack<PortStateRollback> rollbacks)
            throws Exception {
        while (!rollbacks.isEmpty()) {
            rollbacks.pop().doRollback();
        }
    }

    private HostState getHostState(String hostId) {
        return null;
    }

    private void addPortToHost(String hostId) {
        HostState hostState = getHostState(hostId);

        //FIXME: Add port to Host
    }

    private void tryCreatePortState(PortState portState, Stack<PortStateRollback> rollbacks) throws Exception {
        //Verify VPC ID
        VpcRestWrap vpcRestWrap = new VpcRestWrap(vpcRest, rollbacks);
        CompletableFuture vpcFuture = AsyncExecutor.execute(vpcRestWrap::verifyVpc, portState);

        CompletableFuture ipFuture;
        IpAddressRestWrap ipAddressRestWrap = new IpAddressRestWrap(ipAddressRest, rollbacks);

        if (portState.getFixedIps() == null) {
            ipFuture = AsyncExecutor.execute(ipAddressRestWrap::allocateIpAddress, portState);
        } else {
            ipFuture = AsyncExecutor.execute(ipAddressRestWrap::verifyIpAddresses, portState.getFixedIps());
        }

        //Generate uuid for port
        if (portState.getId() == null) {
            portState.setId(UUID.randomUUID().toString());
        }

        CompletableFuture macFuture;
        MacAddressRestWrap macAddressRestWrap = new MacAddressRestWrap(macAddressRest, rollbacks);

        if (portState.getMacAddress() == null) {
            macFuture = AsyncExecutor.execute(macAddressRestWrap::allocateMacAddress, portState);
        } else {
            macFuture = AsyncExecutor.execute(macAddressRestWrap::verifyMacAddress, portState);
        }

        //Verify security group

        //If port binds host, to send it's information to host

        //Wait for all async functions to finish
        CompletableFuture.allOf(vpcFuture, ipFuture, macFuture).join();

        //Persist portState
        portRepository.addItem(portState);
    }


    public PortStateJson createPortState(String projectId, PortStateJson portStateJson) throws Exception {
        LOG.debug("Create port state, projectId: {}, PortStateJson: {}", projectId, portStateJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        PortState portState = portStateJson.getPortState();
        portState.setProjectId(projectId);

        try {
            tryCreatePortState(portState, rollbacks);
        } catch (Exception e) {
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

    private void tryUpdatePortState(String projectId, String portId, PortStateJson portStateJson, Stack<PortStateRollback> rollbacks) throws Exception {
        PortState portState = portStateJson.getPortState();
        PortState oldPortState = portRepository.findItem(portId);

        if (portRepository.findItem(portId) == null) {
            throw new PortStateNotFoundException();
        }

        portState.setProjectId(projectId);

        //Port not changed, nothing to do
        if (portState.equals(oldPortState)) {
            return;
        }

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
        CompletableFuture ipReleaseFuture = null;
        CompletableFuture ipVerifyFuture = null;
        List<PortState.FixedIp> fixedIps = portState.getFixedIps();
        IpAddressRestWrap ipAddressRestWrap = new IpAddressRestWrap(ipAddressRest, rollbacks);

        if (fixedIps != null) {
            List<PortState.FixedIp> oldFixedIps = oldPortState.getFixedIps();

            List<PortState.FixedIp> addFixedIps = fixedIpsCompare(fixedIps, oldFixedIps);
            List<PortState.FixedIp> delFixedIps = fixedIpsCompare(oldFixedIps, fixedIps);

            //releaseIpAddressBulk(delFixedIps, rollbacks);
            if (delFixedIps.size() > 0) {
                ipReleaseFuture = AsyncExecutor.execute(ipAddressRestWrap::releaseIpAddressBulk, delFixedIps);
            }

            //verifyIpAddresses(addFixedIps, rollbacks);
            if (addFixedIps.size() > 0) {
                ipVerifyFuture = AsyncExecutor.execute(ipAddressRestWrap::verifyIpAddresses, addFixedIps);
            }
        } else {
            List<PortState.FixedIp> oldFixedIps = oldPortState.getFixedIps();
            ipVerifyFuture = AsyncExecutor.execute(ipAddressRestWrap::releaseIpAddressBulk, oldFixedIps);
        }

        oldPortState.setFixedIps(fixedIps);

        //Update security_groups
        updateSecurityGroup(oldPortState, portState);

        //Update allow_address_pairs
        //UpdateAllowAddressPairs();

        //Update extra_dhcp_opts
        UpdateExtraDhcpOpts(portState, oldPortState);

        //Update port to host
        updatePortToHost(portState);

        //Wait for all async functions to finish
        if (ipReleaseFuture != null) {
            ipReleaseFuture.join();
        }

        if (ipVerifyFuture != null) {
            ipVerifyFuture.join();
        }

        portRepository.addItem(oldPortState);
        portStateJson.setPortState(oldPortState);
    }

    public PortStateJson updatePortState(String projectId, String portId, PortStateJson portStateJson) throws Exception {
        LOG.debug("Update port state, projectId: {}, portId: {}, PortStateJson: {}",
                projectId, portId, portStateJson);

        Stack<PortStateRollback> rollbacks = new Stack<>();

        try {
            tryUpdatePortState(projectId, portId, portStateJson, rollbacks);
        } catch (Exception e) {
            rollBackAllOperations(rollbacks);
            throw e;
        }

        LOG.debug("Update port state success, portStateJson: {}", portStateJson);

        return portStateJson;
    }

    private void tryDeletePortState(PortState portState, String portId, Stack<PortStateRollback> rollbacks) throws Exception {
        IpAddressRestWrap ipAddressRestWrap = new IpAddressRestWrap(ipAddressRest, rollbacks);
        MacAddressRestWrap macAddressRestWrap = new MacAddressRestWrap(macAddressRest, rollbacks);

        //Release ip address
        //releaseIpAddressBulk(portState.getFixedIps(), rollbacks);
        CompletableFuture ipFuture = null;
        if (portState.getFixedIps() != null && portState.getFixedIps().size() > 0) {
            ipFuture = AsyncExecutor.execute(ipAddressRestWrap::releaseIpAddressBulk, portState.getFixedIps());
        }

        //Release mac address
        //releaseMacAddress(projectId, portState, rollbacks);
        CompletableFuture macFuture = null;
        if (portState.getMacAddress() != null && !"".equals(portState.getMacAddress())) {
            macFuture = AsyncExecutor.execute(macAddressRestWrap::releaseMacAddress, portState);
        }

        //Unbind security groups
        unbindSecurityGroups(portState);

        //Wait for all async functions to finish
        if (ipFuture != null) {
            ipFuture.join();
        }

        if (macFuture != null) {
            macFuture.join();
        }

        portRepository.deleteItem(portId);
    }

    public void deletePortState(String projectId, String portId) throws Exception {
        LOG.debug("Delete port state, projectId: {}, portId: {}", projectId, portId);

        Stack<PortStateRollback> rollbacks = new Stack<>();
        PortState portState = portRepository.findItem(portId);
        if (portState == null) {
            throw new PortStateNotFoundException();
        }

        try {
            tryDeletePortState(portState, portId, rollbacks);
        } catch (Exception e) {
            rollBackAllOperations(rollbacks);
            throw e;
        }

        LOG.debug("Delete port state success, projectId: {}, portId: {}", projectId, portId);
    }

    @Override
    public PortStateJson getPortState(String projectId, String portId) throws Exception {
        PortState portState = portRepository.findItem(portId);
        if (portState == null) {
            throw new PortStateNotFoundException();
        }

        return new PortStateJson(portState);
    }

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
