package com.futurewei.alcor.subnet.service.implement;


import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.subnet.config.IpVersionConfig;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RouteWebObject;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.web.entity.ip.*;
import com.futurewei.alcor.web.entity.subnet.*;
import com.futurewei.alcor.web.entity.mac.*;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
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
    public void ipFallback(int ipVersion, String rangeId, String ipAddr) {
        String ipManagerServiceUrl = ipUrl + ipVersion + "/" + rangeId + "/" + ipAddr;
        restTemplate.delete(ipManagerServiceUrl, IpAddrRequest.class);
        String ipRangeDeleteServiceUrl = ipUrl + "range/" + rangeId;
        restTemplate.delete(ipRangeDeleteServiceUrl, IpAddrRangeRequest.class);
    }

    @Override
    public void fallbackOperation(AtomicReference<RouteWebJson> routeResponseAtomic,
                                  AtomicReference<MacStateJson> macResponseAtomic,
                                  AtomicReference<IpAddrRequest> ipResponseAtomic,
                                  SubnetWebJson resource,
                                  String message) throws CacheException {
        RouteWebJson routeResponse = (RouteWebJson) routeResponseAtomic.get();
        MacStateJson macResponse = (MacStateJson) macResponseAtomic.get();
        IpAddrRequest ipResponse = (IpAddrRequest) ipResponseAtomic.get();
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

        // IP fallback
        logger.info("IP fallback start");
        if (ipResponse != null) {
            this.ipFallback(ipResponse.getIpVersion(), ipResponse.getRangeId(), ipResponse.getIp());
        }
        logger.info("IP fallback end");
    }

    @Override
    public VpcWebJson verifyVpcId(String projectId, String vpcId) throws FallbackException {
        String vpcManagerServiceUrl = vpcUrl + projectId + "/vpcs/" + vpcId;
        VpcWebJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcWebJson.class);
        if (vpcResponse.getNetwork() == null) {
            throw new FallbackException("fallback request");
        }
        return vpcResponse;
    }


    @Override
    public RouteWebJson createRouteRules(String subnetId, SubnetWebObject subnetWebObject) throws FallbackException {
        String routeManagerServiceUrl = routeUrl + "subnets/" + subnetId + "/routes";
        HttpEntity<SubnetWebJson> routeRequest = new HttpEntity<>(new SubnetWebJson(subnetWebObject));
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
    public MacStateJson allocateMacAddressForGatewayPort(String projectId, String vpcId, String portId) throws FallbackException {
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
    public IpAddrRequest allocateIpAddressForGatewayPort(String subnetId, String cidr, String vpcId) throws FallbackException {
        String ipManagerServiceUrl = ipUrl;
        String ipManagerCreateRangeUrl = ipUrl + "range";
        String ipAddressRangeId = UUID.randomUUID().toString();

        // Create Ip Address Range
        // Verify cidr block
        boolean isCidrValid = verifyCidrBlock(cidr);
        if (!isCidrValid) {
            throw new FallbackException("cidr is invalid : " + cidr);
        }

        String[] ips = cidrToFirstIpAndLastIp(cidr);
        if (ips == null || ips.length != 2) {
            throw new FallbackException("cidr transfer to first/last ip failed");
        }
        IpAddrRangeRequest ipAddrRangeRequest = new IpAddrRangeRequest();
        ipAddrRangeRequest.setId(ipAddressRangeId);
        ipAddrRangeRequest.setSubnetId(subnetId);
        ipAddrRangeRequest.setIpVersion(IpVersionConfig.IPV4.getVersion());
        ipAddrRangeRequest.setFirstIp(ips[0]);
        ipAddrRangeRequest.setLastIp(ips[1]);
        ipAddrRangeRequest.setVpcId(vpcId);


        HttpEntity<IpAddrRangeRequest> ipRangeRequest = new HttpEntity<>(new IpAddrRangeRequest(
                ipAddrRangeRequest.getId(),
                ipAddrRangeRequest.getVpcId(),
                ipAddrRangeRequest.getSubnetId(),
                ipAddrRangeRequest.getIpVersion(),
                ipAddrRangeRequest.getFirstIp(),
                ipAddrRangeRequest.getLastIp()));
        IpAddrRangeRequest ipRangeResponse = restTemplate.postForObject(ipManagerCreateRangeUrl, ipRangeRequest, IpAddrRangeRequest.class);
        // retry if ipRangeResponse is null
        if (ipRangeResponse == null) {
            ipRangeResponse = restTemplate.postForObject(ipManagerCreateRangeUrl, ipRangeRequest, IpAddrRangeRequest.class);
        }
        if (ipRangeResponse == null) {
            throw new FallbackException("fallback request");
        }

        // Allocate Ip Address
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(ipRangeResponse.getId());
        ipAddrRequest.setIpVersion(ipRangeResponse.getIpVersion());
        ipAddrRequest.setVpcId(vpcId);
        ipAddrRequest.setSubnetId(subnetId);

        HttpEntity<IpAddrRequest> ipRequest = new HttpEntity<>(new IpAddrRequest(
                ipAddrRequest.getIpVersion(),
                ipAddrRequest.getVpcId(),
                ipAddrRequest.getSubnetId(),
                ipAddrRequest.getRangeId(),
                ipAddrRequest.getIp(),
                ipAddrRequest.getState()));
        IpAddrRequest ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IpAddrRequest.class);
        // retry if ipResponse is null
        if (ipResponse == null) {
            ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IpAddrRequest.class);
        }
        if (ipResponse == null) {
            throw new FallbackException("fallback request");
        }


        return ipResponse;
    }

    @Override
    public String[] cidrToFirstIpAndLastIp(String cidr) {
        if (cidr == null) {
            return null;
        }
        SubnetUtils utils = new SubnetUtils(cidr);
        String highIp = utils.getInfo().getHighAddress();
        String lowIp = utils.getInfo().getLowAddress();
        if (highIp == null || lowIp == null) {
            return null;
        }
        String[] res = new String[2];
        res[0] = lowIp;
        res[1] = highIp;
        return res;
    }

    @Override
    public boolean verifyCidrBlock(String cidr) throws FallbackException {
        if (cidr == null) {
            return false;
        }
        String[] cidrs = cidr.split("\\/", -1);
        // verify cidr suffix
        if (cidrs.length > 2 || cidrs.length == 0) {
            return false;
        } else if (cidrs.length == 2) {
            if (!ControllerUtil.isPositive(cidrs[1])) {
                return false;
            }
            int suffix = Integer.parseInt(cidrs[1]);
            if (suffix < 16 || suffix > 28) {
                return false;
            } else if (suffix == 0 && !"0.0.0.0".equals(cidrs[0])) {
                return false;
            }
        }
        // verify cidr prefix
        String[] addr = cidrs[0].split("\\.", -1);
        if (addr.length != 4) {
            return false;
        }
        for (String f : addr) {
            if (!ControllerUtil.isPositive(f)) {
                return false;
            }
            int n = Integer.parseInt(f);
            if (n < 0 || n > 255) {
                return false;
            }
        }
        return true;

    }

}
