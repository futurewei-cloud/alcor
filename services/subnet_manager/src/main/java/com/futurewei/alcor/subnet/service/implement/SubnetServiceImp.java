package com.futurewei.alcor.subnet.service.implement;


import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.subnet.config.ConstantsConfig;
import com.futurewei.alcor.subnet.config.IpVersionConfig;
import com.futurewei.alcor.subnet.exception.CidrNotWithinNetworkCidr;
import com.futurewei.alcor.subnet.exception.CidrOverlapWithOtherSubnets;
import com.futurewei.alcor.subnet.exception.SubnetIdIsNull;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.utils.SubnetManagementUtil;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
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

import java.util.List;
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
    @DurationStatistics
    public void routeFallback(String routeId, String vpcId) {
        String routeManagerServiceUrl = routeUrl + "vpcs/" + vpcId + "/routes/" + routeId; // for kubernetes test
        restTemplate.delete(routeManagerServiceUrl, ResponseId.class);
    }

    @Async
    @Override
    @DurationStatistics
    public void macFallback(String macAddress) {
        String macManagerServiceUrl = macUrl + "/" + macAddress;
        restTemplate.delete(macManagerServiceUrl, ResponseId.class);
    }

    @Async
    @Override
    @DurationStatistics
    public void ipFallback(String rangeId, String ipAddr) {
        String ipManagerServiceUrl = ipUrl + rangeId + "/" + ipAddr;
        restTemplate.delete(ipManagerServiceUrl);
        String ipRangeDeleteServiceUrl = ipUrl + "range/" + rangeId;
        restTemplate.delete(ipRangeDeleteServiceUrl);
    }

    @Override
    @DurationStatistics
    public void fallbackOperation(AtomicReference<RouteWebJson> routeResponseAtomic,
                                  AtomicReference<MacStateJson> macResponseAtomic,
                                  AtomicReference<IpAddrRequest> ipResponseAtomic,
                                  SubnetRequestWebJson resource,
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
            RouteEntity routeEntity = routeResponse.getRoute();
            this.routeFallback(routeEntity.getId(), resource.getSubnet().getVpcId());
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
            this.ipFallback(ipResponse.getRangeId(), ipResponse.getIp());
        }
        logger.info("IP fallback end");
    }

    @Override
    @DurationStatistics
    public VpcWebJson verifyVpcId(String projectId, String vpcId) throws FallbackException {
        String vpcManagerServiceUrl = vpcUrl + projectId + "/vpcs/" + vpcId;
        VpcWebJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcWebJson.class);
        if (vpcResponse.getNetwork() == null) {
            throw new FallbackException("fallback request");
        }
        return vpcResponse;
    }


    @Override
    @DurationStatistics
    public RouteWebJson createRouteRules(String subnetId, SubnetEntity subnetEntity) throws FallbackException {
        String routeManagerServiceUrl = routeUrl + "subnets/" + subnetId + "/routes";
        HttpEntity<SubnetWebJson> routeRequest = new HttpEntity<>(new SubnetWebJson(subnetEntity));
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
    @DurationStatistics
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
    @DurationStatistics
    public IpAddrRequest allocateIpAddressForGatewayPort(String subnetId, String cidr, String vpcId, String gatewayIp, boolean isOpenToBeAllocated) throws FallbackException {
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

        if (!isOpenToBeAllocated) {
            IpAddrRequest ipAddrRequest = new IpAddrRequest();
            ipAddrRequest.setIpVersion(ipRangeResponse.getIpVersion());
            ipAddrRequest.setRangeId(ipRangeResponse.getId());
            return ipAddrRequest;
        }

        // Allocate Ip Address
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(ipRangeResponse.getId());
        ipAddrRequest.setIpVersion(ipRangeResponse.getIpVersion());
        ipAddrRequest.setIp(gatewayIp);
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
    @DurationStatistics
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

        String[] highIps = highIp.split("\\.");
        String[] lowIps = lowIp.split("\\.");
        Integer high = Integer.parseInt(highIps[highIps.length - 1]) - ConstantsConfig.HighIpInterval;
        Integer low = Integer.parseInt(lowIps[lowIps.length - 1]) + ConstantsConfig.LowIpInterval;
        highIps[highIps.length - 1] = String.valueOf(high);
        lowIps[lowIps.length - 1] = String.valueOf(low);
        highIp = String.join(".", highIps);
        lowIp = String.join(".", lowIps);

        String[] res = new String[2];
        res[0] = lowIp;
        res[1] = highIp;
        return res;
    }

    @Override
    @DurationStatistics
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

    @Override
    @DurationStatistics
    public void addSubnetIdToVpc(String subnetId, String projectId, String vpcId) throws Exception {
        if (subnetId == null) {
            throw new SubnetIdIsNull();
        }

        String vpcManagerServiceUrl = vpcUrl + projectId + "/vpcs/" + vpcId + "/subnets/" + subnetId;
        restTemplate.put(vpcManagerServiceUrl, VpcWebJson.class);

    }

    @Override
    @DurationStatistics
    public void deleteSubnetIdInVpc(String subnetId, String projectId, String vpcId) throws Exception {
        if (subnetId == null) {
            throw new SubnetIdIsNull();
        }

        String vpcManagerServiceUrl = vpcUrl + projectId + "/vpcs/" + vpcId + "/subnetid/" + subnetId;
        restTemplate.put(vpcManagerServiceUrl, VpcWebJson.class);
    }

    @Override
    @DurationStatistics
    public boolean checkIfCidrOverlap(String cidr,String projectId, String vpcId) throws FallbackException, ResourceNotFoundException, ResourcePersistenceException, CidrNotWithinNetworkCidr, CidrOverlapWithOtherSubnets {

        // get vpc and check with vpc cidr
        VpcWebJson vpcWebJson = verifyVpcId(projectId, vpcId);
        String vpcCidr = vpcWebJson.getNetwork().getCidr();

        if (!(vpcCidr == null || vpcCidr.length() == 0)) {
            if (!SubnetManagementUtil.IsCidrWithin(cidr, vpcCidr)) {
                throw new CidrNotWithinNetworkCidr();
            }
        }


        // get subnet list and check with subnets cidr
        List<String> subnetIds = vpcWebJson.getNetwork().getSubnets();
        for (String subnetId : subnetIds) {
            SubnetEntity subnet = this.subnetDatabaseService.getBySubnetId(subnetId);
            if (subnet == null) {
                continue;
            }
            String subnetCidr = subnet.getCidr();
            if (SubnetManagementUtil.IsCidrOverlap(cidr, subnetCidr)) {
                throw new CidrOverlapWithOtherSubnets();
            }
        }

        return false;
    }

}
