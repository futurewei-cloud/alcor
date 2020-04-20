package com.futurewei.alcor.subnet.service.implement;


import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class SubnetServiceImp implements SubnetService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubnetDatabaseService subnetDatabaseService;

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Value("${microservices.mac.service.url}")
    private String macUrl;

    @Value("${microservices.ip.service.url}")
    private String ipUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Async
    @Override
    public void routeFallback(String routeId, String vpcId) {
        String routeManagerServiceUrl = routeUrl + "vpcs/" + vpcId + "/routes/" + routeId; // for kubernetes test
        restTemplate.delete(routeManagerServiceUrl, ResponseId.class);
    }

    @Async
    @Override
    public void macFallback(String macAddress) {
        String macManagerServiceUrl = macUrl + "/" + macAddress;
        restTemplate.delete(macManagerServiceUrl, ResponseId.class);
    }

    @Async
    @Override
    public void ipFallback(String ipGateway) {
        String ipManagerServiceUrl = ipUrl + ipGateway;
        restTemplate.delete(ipManagerServiceUrl, ResponseId.class);
    }

    @Override
    public void fallbackOperation(AtomicReference<RouteWebJson> routeResponseAtomic,
                                  AtomicReference<MacStateJson> macResponseAtomic,
                                  SubnetStateJson resource,
                                  String message) {
        RouteWebJson routeResponse = (RouteWebJson) routeResponseAtomic.get();
        MacStateJson macResponse = (MacStateJson) macResponseAtomic.get();
        logger.error(message);

        // Subnet fallback
        logger.info("subnet fallback start");
        this.subnetDatabaseService.deleteSubnet(resource.getSubnet().getId());
        logger.info("subnet fallback end");

        // Route fallback
        logger.info("Route fallback start");
        if (routeResponse != null) {
            RouteWebObject route = routeResponse.getRoute();
            this.routeFallback(route.getId(), resource.getSubnet().getVpcId());
        }
        logger.info("Route fallback end");

        // Mac fallback
        logger.info("Mac fallback start");
        if (macResponse != null) {
            this.macFallback(macResponse.getMacState().getMacAddress());
        }
        logger.info("Mac fallback end");
    }

    @Override
    public VpcStateJson verifyVpcId(String projectid, String vpcId) throws FallbackException {
        String vpcManagerServiceUrl = vpcUrl + projectid + "/vpcs/" + vpcId;
        VpcStateJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcStateJson.class);
        if (vpcResponse.getVpc() == null) {
            throw new FallbackException("fallback request");
        }
        return vpcResponse;
    }

    @Override
    public RouteWebJson createRouteRules(String subnetId, SubnetState subnetState) throws FallbackException {
        String routeManagerServiceUrl = routeUrl + "subnets/" + subnetId + "/routes";
        HttpEntity<SubnetStateJson> routeRequest = new HttpEntity<>(new SubnetStateJson(subnetState));
        RouteWebJson routeResponse = restTemplate.postForObject(routeManagerServiceUrl, routeRequest, RouteWebJson.class);
        // retry if routeResponse is null
        if (routeResponse == null) {
            routeResponse = restTemplate.postForObject(routeManagerServiceUrl, routeRequest, RouteWebJson.class);
        }
        if (routeResponse == null) {
            throw new FallbackException("fallback request");
        }
        return routeResponse;
    }

    @Override
    public MacStateJson allocateMacGateway(String projectId, String vpcId, String portId) throws FallbackException {
        String macManagerServiceUrl = macUrl;
        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setPortId(portId);
        macState.setVpcId(vpcId);

        HttpEntity<MacStateJson> macRequest = new HttpEntity<>(new MacStateJson(macState));
        MacStateJson macResponse = restTemplate.postForObject(macManagerServiceUrl, macRequest, MacStateJson.class);
        // retry if macResponse is null
        if (macResponse == null) {
            macResponse = restTemplate.postForObject(macManagerServiceUrl, macRequest, MacStateJson.class);
        }
        if (macResponse == null) {
            throw new FallbackException("fallback request");
        }
        return macResponse;
    }

    @Override
    public IPStateJson allocateIPGateway(String subnetId, String cidr, String portId) throws FallbackException {
        IPState ipState = new IPState();
        ipState.setSubnetId(subnetId);
        ipState.setPortId(portId);
        ipState.setSubnetCidr(cidr);

        String ipManagerServiceUrl = ipUrl;
        HttpEntity<IPStateJson> ipRequest = new HttpEntity<>(new IPStateJson(ipState));
        IPStateJson ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IPStateJson.class);
        // retry if ipResponse is null
        if (ipResponse == null) {
            ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IPStateJson.class);
        }
        if (ipResponse == null) {
            throw new FallbackException("fallback request");
        }
        return ipResponse;
    }
}
