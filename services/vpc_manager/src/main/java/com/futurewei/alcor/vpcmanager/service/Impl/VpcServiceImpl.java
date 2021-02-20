package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.web.entity.gateway.VpcInfo;
import com.futurewei.alcor.web.entity.gateway.VpcInfoJson;
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

import java.util.List;
import java.util.UUID;

@Service
public class VpcServiceImpl implements VpcService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SegmentService segmentService;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Value("${microservices.gateway.service.url}")
    private String gatewayUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Get route rule info
     * @param vpcId
     * @param vpcState
     * @return route state
     */
    @Override
    @DurationStatistics
    public RouteWebJson getRoute(String vpcId, VpcEntity vpcState) {
        String routeManagerServiceUrl = routeUrl + "vpcs/" + vpcId + "/routes";
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
    @DurationStatistics
    public VpcEntity allocateSegmentForNetwork(VpcEntity vpcEntity) throws Exception {
        String networkTypeId = UUID.randomUUID().toString();
        if (vpcEntity == null) {
            return vpcEntity;
        }

        String networkType = vpcEntity.getNetworkType();
        Long key = null;
        if (networkType == null) {
            // create a vxlan type segment as default
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());

            vpcEntity.setNetworkType(NetworkTypeEnum.VXLAN.getNetworkType());

        } else if (networkType.equals(NetworkTypeEnum.VXLAN.getNetworkType())) {
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        } else if (networkType.equals(NetworkTypeEnum.VLAN.getNetworkType())) {
            key = this.segmentService.addVlanEntity( networkTypeId, NetworkTypeEnum.VLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        } else if (networkType.equals(NetworkTypeEnum.GRE.getNetworkType())) {
            key = this.segmentService.addGreEntity( networkTypeId, NetworkTypeEnum.GRE.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        }

        if (key != null) {
            vpcEntity.setSegmentationId(Integer.parseInt(String.valueOf(key)));
        }

        return vpcEntity;
    }

    /**
     * check subnets in network are empty or not
     * @param vpcEntity
     * @return
     * @throws SubnetsNotEmptyException
     */
    @Override
    @DurationStatistics
    public boolean checkSubnetsAreEmpty(VpcEntity vpcEntity) throws SubnetsNotEmptyException {
        if (vpcEntity == null) {
            return true;
        }
        List<String> subnets = vpcEntity.getSubnets();
        if (!(subnets == null || subnets.size() == 0)) {
            throw new SubnetsNotEmptyException();
        }
        return true;
    }

    @Override
    public ResponseId registerVpc(VpcEntity vpcEntity) {
        String url = gatewayUrl + "project/" + vpcEntity.getProjectId() + "/gatewayinfo";
        VpcInfo vpcInfo = new VpcInfo(vpcEntity.getId(), vpcEntity.getSegmentationId(), vpcEntity.getProjectId());
        HttpEntity<VpcInfoJson> vpcHttpEntity = new HttpEntity<>(new VpcInfoJson(vpcInfo));
        return restTemplate.postForObject(url, vpcHttpEntity, ResponseId.class);
    }

    @Override
    public ResponseId unRegisterVpc(VpcEntity vpcEntity) {
        String url = gatewayUrl + "project/" + vpcEntity.getProjectId() + "/gatewayinfo/" + vpcEntity.getId();
        restTemplate.delete(url);
        return new ResponseId(vpcEntity.getId());
    }
}
