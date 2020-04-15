package com.futurewei.alcor.subnet.service.implement;


import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.service.SubnetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
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
    public void routeRollback(String routeId, String vpcId) {
        String routeManagerServiceUrl = routeUrl + vpcId + "/routes/" + routeId; // for kubernetes test
        restTemplate.delete(routeManagerServiceUrl, ResponseId.class);
    }

    @Async
    @Override
    public VpcStateJson verifyVpcId(String projectid, String vpcId) throws FallbackException {
        String vpcManagerServiceUrl = vpcUrl + projectid + "/vpcs/" + vpcId; // for kubernetes test
        //HttpEntity<SubnetStateJson> vpcRequest = new HttpEntity<>(new SubnetStateJson(subnetState));
        VpcStateJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcStateJson.class);
        if (vpcResponse.getVpc() == null) {
            throw new FallbackException("fallback request");
        }
        return vpcResponse;
    }

    //@Async
    @Override
    public RouteWebJson createRouteRules(String subnetId, SubnetState subnetState) throws FallbackException {
        String routeManagerServiceUrl = routeUrl + subnetId + "/routes"; // for kubernetes test
        HttpEntity<SubnetStateJson> routeRequest = new HttpEntity<>(new SubnetStateJson(subnetState));
        RouteWebJson routeResponse = restTemplate.postForObject(routeManagerServiceUrl, routeRequest, RouteWebJson.class);
        if (routeResponse == null) {
            throw new FallbackException("fallback request");
        }
        return routeResponse;
    }

    @Override
    public MacStateJson allocateMacGateway(String projectId, String vpcId, String portId) {
        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setPortId(portId);
        macState.setVpcId(vpcId);

        HttpEntity<MacStateJson> macRequest = new HttpEntity<>(new MacStateJson(macState));
        MacStateJson macResponse = restTemplate.postForObject(macUrl, macRequest, MacStateJson.class);
        return macResponse;
    }

    @Override
    public IPStateJson allocateIPGateway(String subnetId, String cidr, String portId) {
        IPState ipState = new IPState();
        ipState.setSubnetId(subnetId);
        ipState.setPortId(portId);
        ipState.setSubnetCidr(cidr);

        String ipManagerServiceUrl = ipUrl + subnetId + "/routes"; // for kubernetes test
        HttpEntity<IPStateJson> ipRequest = new HttpEntity<>(new IPStateJson(ipState));
        IPStateJson ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IPStateJson.class);
        return ipResponse;
    }
}
