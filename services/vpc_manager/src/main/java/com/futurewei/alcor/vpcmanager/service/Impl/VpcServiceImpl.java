package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class VpcServiceImpl implements VpcService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SegmentService segmentService;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Get route rule info
     * @param vpcId
     * @param vpcState
     * @return route state
     */
    @Override
    public RouteWebJson getRoute(String vpcId, VpcEntity vpcState) {
        String routeManagerServiceUrl = routeUrl + vpcId + "/routes";
        HttpEntity<VpcWebJson> request = new HttpEntity<>(new VpcWebJson(vpcState));
        RouteWebJson response = restTemplate.postForObject(routeManagerServiceUrl, request, RouteWebJson.class);
        return response;
    }

    /**
     * Allocate a segment for the network
     * @param vpcEntity
     * @return
     * @throws Exception
     */
    @Override
    public VpcEntity allocateSegmentForNetwork(VpcEntity vpcEntity) throws Exception {
        String networkTypeId = UUID.randomUUID().toString();
        if (vpcEntity == null) {
            return vpcEntity;
        }

        String networkType = vpcEntity.getNetworkType();
        Long key = null;
        if (networkType == null) {
            // create a vxlan type segment as default
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId());

            vpcEntity.setNetworkType(NetworkTypeEnum.VXLAN.getNetworkType());

        } else if (networkType.equals(NetworkTypeEnum.VXLAN.getNetworkType())) {
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId());
        } else if (networkType.equals(NetworkTypeEnum.VLAN.getNetworkType())) {
            key = this.segmentService.addVlanEntity( networkTypeId, NetworkTypeEnum.VLAN.getNetworkType(), vpcEntity.getId());
        } else if (networkType.equals(NetworkTypeEnum.GRE.getNetworkType())) {
            key = this.segmentService.addGreEntity( networkTypeId, NetworkTypeEnum.GRE.getNetworkType(), vpcEntity.getId());
        }

        if (key != null) {
            vpcEntity.setSegmentationId(Integer.parseInt(String.valueOf(key)));
        }

        return vpcEntity;
    }
}
