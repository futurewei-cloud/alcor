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


import com.futurewei.alcor.common.entity.*;
import com.futurewei.alcor.common.rest.IpAddressRest;
import com.futurewei.alcor.common.rest.MacAddressRest;
import com.futurewei.alcor.common.rest.VpcRest;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.portmanager.utils.Ipv4AddrUtil;
import com.futurewei.alcor.portmanager.utils.Ipv6AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ComponentScan(value="com.futurewei.alcor.common.rest")
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

    private int getIpVersion(String ipAddress) throws Exception {
        if (Ipv4AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV4.getVersion();
        } else if (Ipv6AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV6.getVersion();
        } else {
            throw new IpAddrInvalidException();
        }
    }

    private String getRangeIdBySubnetId(String subnetId, int ipVersion) throws Exception {
        if (IpVersion.IPV4.getVersion() == ipVersion) {
            return subnetId; //FIXME:return the right rangeId get from subnet manager
        } else if (IpVersion.IPV6.getVersion() == ipVersion) {
            return subnetId; //FIXME:return the right rangeId get from subnet manager
        }

        throw new IpVersionInvalidException();
    }

    private void verifyIpAddresses(List<PortState.FixedIp> fixedIps, Stack<PortStateRollback> rollbacks) throws Exception {
        for (PortState.FixedIp fixedIp: fixedIps) {
            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);

            IpAddrRequest result = ipAddressRest.allocateIpAddress(rangeId, fixedIp.getIpAddress());

            AllocateIpAddrRollback ipAddressRollback = new AllocateIpAddrRollback(ipAddressRest);
            ipAddressRollback.putAllocatedIpAddress(result);
            rollbacks.push(ipAddressRollback);
        }
    }

    private String getRangeIdForPort(String vpcId) {
        return "range1";
    }

    private void allocateIpAddress(PortState portState, Stack<PortStateRollback> rollbacks) throws Exception {
        String rangeId = getRangeIdForPort(portState.getVpcId());

        IpAddrRequest result = ipAddressRest.allocateIpAddress(rangeId, null);

        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        PortState.FixedIp fixedIp = new PortState.FixedIp();

        fixedIp.setSubnetId(result.getSubnetId());
        fixedIp.setIpAddress(result.getIp());
        fixedIps.add(fixedIp);

        portState.setFixedIps(fixedIps);

        AllocateIpAddrRollback ipAddressRollback = new AllocateIpAddrRollback(ipAddressRest);
        ipAddressRollback.putAllocatedIpAddress(result);
        rollbacks.push(ipAddressRollback);
    }

    private void allocateMacAddress(String projectId, PortState portState, Stack<PortStateRollback> rollbacks) throws Exception {
        MacStateJson result = macAddressRest.allocateMacAddress(projectId, portState.getVpcId(), portState.getId());
        portState.setMacAddress(result.getMacState().getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setVpcId(portState.getVpcId());
        macState.setMacAddress(portState.getMacAddress());

        AllocateMacAddrRollback rollback = new AllocateMacAddrRollback(macAddressRest);
        rollback.putAllocatedMacAddress(macState);

        rollbacks.push(rollback);
    }

    private void verifyMacAddress(String projectId, PortState portState, Stack<PortStateRollback> rollbacks) {
        //FIXME: Not support yet
    }

    private void releaseMacAddress(String projectId, PortState portState, Stack<PortStateRollback> rollbacks) throws Exception {
        macAddressRest.releaseMacAddress(portState.getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setVpcId(portState.getVpcId());
        macState.setMacAddress(portState.getMacAddress());

        ReleaseMacAddrRollback rollback = new ReleaseMacAddrRollback(macAddressRest);
        rollback.putReleasedMacAddress(macState);

        rollbacks.push(rollback);
    }

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

    private void setDefaultValue(PortState portState) {
        if (portState.getId() == null) {
            portState.setId("");
        }

        if (portState.getProjectId() == null) {
            portState.setProjectId("");
        }

        if (portState.getName() == null) {
            portState.setName("");
        }
        if (portState.getDescription() == null) {
            portState.setDescription("");
        }
        if (portState.getVpcId() == null) {
            portState.setVpcId("");
        }

        if (portState.getTenantId() == null) {
            portState.setTenantId("");
        }

        if (portState.getMacAddress() == null) {
            portState.setMacAddress("");
        }

        if (portState.getVethName() == null) {
            portState.setVethName("");
        }

        if (portState.getDeviceId() == null) {
            portState.setDeviceId("");
        }

        if (portState.getDeviceOwner() == null) {
            portState.setDeviceOwner("");
        }

        if (portState.getStatus() == null) {
            portState.setStatus("");
        }

        if (portState.getFixedIps() == null) {
            portState.setFixedIps(new ArrayList<>());
        }

        if (portState.getAllowedAddressPairs() == null) {
            portState.setAllowedAddressPairs(new ArrayList<>());
        }

        if (portState.getExtraDhcpOpts() == null) {
            portState.setExtraDhcpOpts(new ArrayList<>());
        }

        if (portState.getSecurityGroups() == null) {
            portState.setSecurityGroups(new ArrayList<>());
        }

        if (portState.getBindingHostId() == null) {
            portState.setBindingHostId("");
        }

        if (portState.getBindingProfile() == null) {
            portState.setBindingProfile("");
        }

        if (portState.getBindingVnicType() == null) {
            portState.setBindingVnicType("");
        }

        if (portState.getNetworkNamespace() == null) {
            portState.setNetworkNamespace("");
        }

        if (portState.getDnsName() == null) {
            portState.setDnsName("");
        }

        if (portState.getDnsAssignment() == null) {
            portState.setDnsAssignment(new ArrayList<>());
        }
    }

    private PortStateJson tryCreatePortState(String projectId, PortState portState, Stack<PortStateRollback> rollbacks) throws Exception {
        //Ensure that the vpc exists
        vpcRest.verifyVpc(projectId, portState.getVpcId());

        //Allocate ip address for port
        List<PortState.FixedIp> fixedIps = portState.getFixedIps();
        if (fixedIps == null) {
            allocateIpAddress(portState, rollbacks);
        } else {
            verifyIpAddresses(fixedIps, rollbacks);
        }

        //Generate uuid for port
        if (portState.getId() == null) {
            portState.setId(UUID.randomUUID().toString());
        }

        //Allocate mac address for port
        if (portState.getMacAddress() == null) {
            allocateMacAddress(projectId, portState, rollbacks);
        } else {
            verifyMacAddress(projectId, portState, rollbacks);
        }

        //Verify security group
        List<String> securityGroups = portState.getSecurityGroups();
        if (securityGroups == null) {
            getDefaultSecurityGroup(portState);
        } else {
            bindSecurityGroups(portState);
        }

        //If port binds host, to send it's information to host
        String bindingHostId = portState.getBindingHostId();
        if (bindingHostId != null) {
            addPortToHost(bindingHostId);
        }

        portState.setProjectId(projectId);
        setDefaultValue(portState);

        portRepository.addItem(portState);

        return new PortStateJson(portState);
    }

    public PortStateJson createPortState(String projectId, PortStateJson portStateJson) throws Exception {
        LOG.debug("Create port state, projectId: {}, PortStateJson: {}", projectId, portStateJson);
        Stack<PortStateRollback> rollbacks = new Stack<>();

        try {
            tryCreatePortState(projectId, portStateJson.getPortState(), rollbacks);
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

    private void releaseIpAddressBulk(List<PortState.FixedIp> fixedIps, Stack<PortStateRollback> rollbacks) throws Exception {
        for (PortState.FixedIp fixedIp: fixedIps) {
            ipAddressRest.releaseIpAddress(fixedIp.getSubnetId(), fixedIp.getIpAddress());

            IpAddrRequest ipAddrRequest = new IpAddrRequest();

            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            ipAddrRequest.setRangeId(rangeId);
            ipAddrRequest.setIp(fixedIp.getIpAddress());

            ReleaseIpAddrRollback ipAddressRollback = new ReleaseIpAddrRollback(ipAddressRest);
            ipAddressRollback.putReleasedIpAddress(ipAddrRequest);
            rollbacks.push(ipAddressRollback);
        }
    }

    private void updateIpAddress(PortState portState, PortState portStateOld, Stack<PortStateRollback> rollbacks) throws Exception {
        List<PortState.FixedIp> fixedIps = portState.getFixedIps();
        List<PortState.FixedIp> oldFixedIps = portStateOld.getFixedIps();

        List<PortState.FixedIp> addFixedIps = fixedIpsCompare(fixedIps, oldFixedIps);
        List<PortState.FixedIp> delFixedIps = fixedIpsCompare(oldFixedIps, fixedIps);

        releaseIpAddressBulk(delFixedIps, rollbacks);

        verifyIpAddresses(addFixedIps, rollbacks);

        portStateOld.setFixedIps(fixedIps);
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

    private PortStateJson tryUpdatePortState(String projectId, String portId, PortStateJson portStateJson, Stack<PortStateRollback> rollbacks) throws Exception {
        PortState portState = portStateJson.getPortState();
        PortState oldPortState = portRepository.findItem(portId);

        if (portRepository.findItem(portId) == null) {
            throw new PortStateNotFoundException();
        }

        //Port not changed, nothing to do
        portState.setProjectId(projectId);
        setDefaultValue(portState);
        if (portState.equals(oldPortState)) {
            return portStateJson;
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
        if (portState.getFixedIps() != null) {
            updateIpAddress(portState, oldPortState, rollbacks);
        }

        //Update security_groups
        updateSecurityGroup(oldPortState, portState);

        //Update allow_address_pairs
        //UpdateAllowAddressPairs();

        //Update extra_dhcp_opts
        UpdateExtraDhcpOpts(portState, oldPortState);

        //Update port to host
        updatePortToHost(portState);

        portRepository.addItem(oldPortState);

        return portStateJson;
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

    private void tryDeletePortState(PortState portState, String projectId, String portId, Stack<PortStateRollback> rollbacks) throws Exception {
        //Release ip address
        releaseIpAddressBulk(portState.getFixedIps(), rollbacks);

        //Release mac address
        releaseMacAddress(projectId, portState, rollbacks);

        //Unbind security groups
        unbindSecurityGroups(portState);

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
            tryDeletePortState(portState, projectId, portId, rollbacks);
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
