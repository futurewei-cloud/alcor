package com.futurewei.alcor.subnet.service.implement;


import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.RouteWebJson;
import com.futurewei.alcor.subnet.entity.SubnetState;
import com.futurewei.alcor.subnet.entity.VpcStateJson;
import com.futurewei.alcor.subnet.service.SubnetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubnetServiceImp implements SubnetService {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Value("${microservices.mac.service.url}")
    private String macUrl;

    @Value("${microservices.ip.service.url}")
    private String ipUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public VpcStateJson verifyVpcId(String projectid, SubnetState inSubnetState) {
        String vpcManagerServiceUrl = vpcUrl + projectid + "/vpcs/" + inSubnetState.getVpcId(); // for kubernetes test
        //HttpEntity<SubnetStateJson> vpcRequest = new HttpEntity<>(new SubnetStateJson(subnetState));
        VpcStateJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcStateJson.class);
        return vpcResponse;
    }

    @Override
    public RouteWebJson prepeareRouteRule(SubnetState inSubnetState, VpcStateJson vpcResponse) {
        String routeManagerServiceUrl = routeUrl + inSubnetState.getId() + "/routes"; // for kubernetes test
        HttpEntity<VpcStateJson> routeRequest = new HttpEntity<>(new VpcStateJson(vpcResponse.getVpc()));
        RouteWebJson routeResponse = restTemplate.postForObject(routeManagerServiceUrl, routeRequest, RouteWebJson.class);
        return routeResponse;
    }
}
