package com.futurewei.alcor.dataplane.client;

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.SecurityGroupPortsCache;
import com.futurewei.alcor.dataplane.cache.VpcClientStatusCache;
import com.futurewei.alcor.dataplane.cache.VpcSubnetsCache;
import com.futurewei.alcor.dataplane.client.grpc.DataPlaneClientImpl;
import com.futurewei.alcor.dataplane.config.ClientConstant;
import com.futurewei.alcor.dataplane.entity.InternalPorts;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class ClientManagerImpl implements ClientManager {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private LocalCache localCache;

    @Autowired
    private VpcSubnetsCache vpcSubnetsCache;

    @Autowired
    private SecurityGroupPortsCache securityGroupPortsCache;

    @Autowired
    private VpcClientStatusCache vpcClientStatusCache;

    @Override
    public boolean isFastPath(InternalPortEntity portEntity) throws Exception {
        String correspondingVpcId = portEntity.getVpcId();
        List<String> subnetsId = vpcSubnetsCache.getVpcSubnets(correspondingVpcId).getSubnetIds();

        //      Get the number of VPC's ports
        int numberOfVpcPorts = 0;
        for (String subnetId : subnetsId) {
            numberOfVpcPorts += localCache.getSubnetPorts(subnetId).getPorts().size();
        }

        //      Get the max number of SecurityGroup's ports
        List<Integer> numberOfSecurityGroupPorts = new ArrayList<>();
        for (String securityGroupId : portEntity.getSecurityGroups()) {
            numberOfSecurityGroupPorts.add(securityGroupPortsCache.getSecurityGroupPorts(securityGroupId).getPortIds().size());
        }
        Collections.sort(numberOfSecurityGroupPorts);
        int maxNumberOfSGPorts = numberOfSecurityGroupPorts.get(numberOfSecurityGroupPorts.size() - 1);

        //      Select client for port entity
        String vpcCurrentClient = vpcClientStatusCache.getClientStatusByVpcId(correspondingVpcId);

        if (vpcCurrentClient == ClientConstant.fastPath) {
            if ((numberOfVpcPorts < ClientConstant.X) && (maxNumberOfSGPorts < ClientConstant.Y)) {
                return true;
            } else {
                return false;
            }
        } else {
            if ((numberOfVpcPorts < (0.8 * ClientConstant.X)) && (maxNumberOfSGPorts < (0.8 * ClientConstant.Y))) {
                return true;
            } else {
                return false;
            }
        }
    }
}
