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
package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.vpcmanager.service.NetworkIPAvailabilityService;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.IPUtil;
import com.futurewei.alcor.web.entity.subnet.AllocationPool;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilitiesWebJson;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilityEntity;
import com.futurewei.alcor.web.entity.vpc.SubnetIPAvailabilityEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NetworkIPAvailabilityServiceImpl implements NetworkIPAvailabilityService {

    @Autowired
    VpcDatabaseService vpcDatabaseService;

    @Override
    public NetworkIPAvailabilityEntity getNetworkIPAvailability(String vpcid) throws ResourceNotFoundException, ResourcePersistenceException {

        NetworkIPAvailabilityEntity networkIPAvailabilityEntity = new NetworkIPAvailabilityEntity();

        VpcEntity vpcEntity = this.vpcDatabaseService.getByVpcId(vpcid);
        if (vpcEntity == null) {
            throw new ResourceNotFoundException("vpc can not be found");
        }

        networkIPAvailabilityEntity = setNetworkIPAvailability(vpcEntity);

        return networkIPAvailabilityEntity;
    }

    @Override
    public List<NetworkIPAvailabilityEntity> getNetworkIPAvailabilities() throws CacheException, ResourceNotFoundException {

        List<NetworkIPAvailabilityEntity> networkIPAvailabilityEntities = new ArrayList<NetworkIPAvailabilityEntity>();

        Map<String, VpcEntity> vpcStates = this.vpcDatabaseService.getAllVpcs();

        // TODO: filter vpcStates by request parameters

        for (Map.Entry<String, VpcEntity> entry : vpcStates.entrySet()) {
            VpcEntity vpcEntity = (VpcEntity) entry.getValue();
            NetworkIPAvailabilityEntity networkIPAvailabilityEntity = setNetworkIPAvailability(vpcEntity);
            networkIPAvailabilityEntities.add(networkIPAvailabilityEntity);
        }

        return networkIPAvailabilityEntities;
    }

    @Override
    public NetworkIPAvailabilityEntity setNetworkIPAvailability(VpcEntity vpcEntity) throws ResourceNotFoundException {

        NetworkIPAvailabilityEntity networkIPAvailabilityEntity = new NetworkIPAvailabilityEntity();

        // set up properties value related with vpc
        List<SubnetEntity> subnets = vpcEntity.getSubnets();
        List<SubnetIPAvailabilityEntity> subnetIpAvailability = new ArrayList<SubnetIPAvailabilityEntity>();
        if (subnets == null) {
            networkIPAvailabilityEntity.setSubnetIpAvailability(subnetIpAvailability);
        }else {
            for (SubnetEntity subnetEntity : subnets) {
                SubnetIPAvailabilityEntity subnetIPAvailabilityEntity = new SubnetIPAvailabilityEntity();

                if (subnetEntity == null) {
                    throw new ResourceNotFoundException("subnet can not be found");
                }

                // set up value of total_ips
                List<AllocationPool> allocationPools = subnetEntity.getAllocationPools();
                int totalIPs = 0;
                if (allocationPools == null) {
                    throw new ResourceNotFoundException("allocation pools can not be found");
                }
                for (AllocationPool allocationPool : allocationPools) {
                    String startIP = allocationPool.getStart();
                    String endIP = allocationPool.getEnd();
                    if (startIP == null || endIP == null) {
                        continue;
                    }
                    int ips = IPUtil.countIpNumberByStartIpAndEndIp(startIP, endIP);
                    totalIPs += ips;
                }

                // TODO: set up value of used_ips
                subnetIPAvailabilityEntity.setUsedIps(1);
                subnetIPAvailabilityEntity.setTotalIps(totalIPs);
                subnetIPAvailabilityEntity.setCidr(subnetEntity.getCidr());
                subnetIPAvailabilityEntity.setIpVersion(subnetEntity.getIpVersion());
                subnetIPAvailabilityEntity.setSubnetId(subnetEntity.getId());
                subnetIPAvailabilityEntity.setSubnetName(subnetEntity.getName());

                subnetIpAvailability.add(subnetIPAvailabilityEntity);
            }
        }

        int totalIps = 0;
        int usedIps = 0;
        for (SubnetIPAvailabilityEntity subnetIPAvailabilityEntity : subnetIpAvailability) {
            totalIps += subnetIPAvailabilityEntity.getTotalIps();
            usedIps += subnetIPAvailabilityEntity.getUsedIps();
        }

        networkIPAvailabilityEntity.setSubnetIpAvailability(subnetIpAvailability);
        networkIPAvailabilityEntity.setTotalIps(totalIps);
        networkIPAvailabilityEntity.setUsedIps(usedIps);
        networkIPAvailabilityEntity.setVpcId(vpcEntity.getId());
        networkIPAvailabilityEntity.setVpcName(vpcEntity.getName());
        networkIPAvailabilityEntity.setTenantId(vpcEntity.getTenantId());
        networkIPAvailabilityEntity.setProjectId(vpcEntity.getProjectId());

        return networkIPAvailabilityEntity;
    }
}
