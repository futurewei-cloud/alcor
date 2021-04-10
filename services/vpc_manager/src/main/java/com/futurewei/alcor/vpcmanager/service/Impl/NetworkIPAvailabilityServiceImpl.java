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

package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.vpcmanager.service.NetworkIPAvailabilityService;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.IPUtil;
import com.futurewei.alcor.web.entity.subnet.AllocationPool;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilityEntity;
import com.futurewei.alcor.web.entity.vpc.SubnetIPAvailabilityEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NetworkIPAvailabilityServiceImpl implements NetworkIPAvailabilityService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    VpcDatabaseService vpcDatabaseService;

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * get network ip availability by vpc id
     * @param vpcid
     * @return
     * @throws ResourceNotFoundException
     * @throws ResourcePersistenceException
     */
    @Override
    public NetworkIPAvailabilityEntity getNetworkIPAvailability(String vpcid) throws ResourceNotFoundException, ResourcePersistenceException {

        NetworkIPAvailabilityEntity networkIPAvailabilityEntity = new NetworkIPAvailabilityEntity();

        VpcEntity vpcEntity = this.vpcDatabaseService.getByVpcId(vpcid);
        if (vpcEntity == null) {
            throw new ResourceNotFoundException("vpc can not be found" + vpcid);
        }

        networkIPAvailabilityEntity = setNetworkIPAvailability(vpcEntity);

        return networkIPAvailabilityEntity;
    }

    /**
     * list all network ip availabilities
     * @param queryParams
     * @return
     * @throws CacheException
     * @throws ResourceNotFoundException
     */
    @Override
    public List<NetworkIPAvailabilityEntity> getNetworkIPAvailabilities(Map<String, Object[]> queryParams) throws CacheException, ResourceNotFoundException {

        List<NetworkIPAvailabilityEntity> networkIPAvailabilityEntities = new ArrayList<NetworkIPAvailabilityEntity>();

        Map<String, VpcEntity> vpcStates = this.vpcDatabaseService.getAllVpcs(queryParams);

        for (Map.Entry<String, VpcEntity> entry : vpcStates.entrySet()) {
            VpcEntity vpcEntity = (VpcEntity) entry.getValue();
            NetworkIPAvailabilityEntity networkIPAvailabilityEntity = setNetworkIPAvailability(vpcEntity);
            networkIPAvailabilityEntities.add(networkIPAvailabilityEntity);
        }

        return networkIPAvailabilityEntities;
    }

    /**
     * set up network ip availabilities with vpc state
     * @param vpcEntity
     * @return
     * @throws ResourceNotFoundException
     */
    @Override
    public NetworkIPAvailabilityEntity setNetworkIPAvailability(VpcEntity vpcEntity) throws ResourceNotFoundException {

        NetworkIPAvailabilityEntity networkIPAvailabilityEntity = new NetworkIPAvailabilityEntity();

        // set up properties value related with vpc
        List<String> subnetIds = vpcEntity.getSubnets();
        List<SubnetIPAvailabilityEntity> subnetIpAvailability = new ArrayList<SubnetIPAvailabilityEntity>();
        if (subnetIds == null) {
            networkIPAvailabilityEntity.setSubnetIpAvailability(subnetIpAvailability);
        }else {
            List<SubnetEntity> subnets = getSubnets(subnetIds);
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

                // set up value of used_ips
                Integer ipVersion = subnetEntity.getIpVersion();
                String rangeId = "";
                if (ipVersion == 4) {
                    rangeId = subnetEntity.getIpV4RangeId();
                }else {
                    rangeId = subnetEntity.getIpV6RangeId();
                }

                Integer usedIps = getUsedIps(rangeId);

                // TODO: set up value of used_ips
                subnetIPAvailabilityEntity.setUsedIps(usedIps);
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

    /**
     * get subnets by subnet ids
     * @param subnets
     * @return list of subnet state
     * @throws ResourceNotFoundException
     */
    @Override
    public List<SubnetEntity> getSubnets(List<String> subnets) throws ResourceNotFoundException {

        List<SubnetEntity> subnetEntities = new ArrayList<>();

        for (String subnetId : subnets) {
            String subnetManagerServiceUrl = subnetUrl + "/subnets/" + subnetId;
            SubnetWebJson subnetResponse = restTemplate.getForObject(subnetManagerServiceUrl, SubnetWebJson.class);
            if (subnetResponse == null) {
                throw new ResourceNotFoundException("The subnet can not be found by subnet Id");
            }
            SubnetEntity subnet = subnetResponse.getSubnet();
            subnetEntities.add(subnet);
        }

        return subnetEntities;
    }

    /**
     * get used_ips by range id
     * @param rangeId
     * @return integer
     */
    @Override
    public Integer getUsedIps(String rangeId) {
        String subnetManagerServiceUrl = subnetUrl + "/subnets/" + rangeId;
        Integer usedIps = restTemplate.getForObject(subnetManagerServiceUrl, Integer.class);
        if (usedIps == null) {
            logger.info("used_ips is null");
        }
        return usedIps;
    }
}
