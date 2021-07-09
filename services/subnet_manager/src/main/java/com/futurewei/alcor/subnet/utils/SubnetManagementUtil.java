/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.subnet.utils;

import com.futurewei.alcor.common.enumClass.Ipv6AddressModeEnum;
import com.futurewei.alcor.common.enumClass.Ipv6RaModeEnum;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.subnet.config.ConstantsConfig;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.service.implement.SubnetServiceImp;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import com.futurewei.alcor.web.entity.subnet.SubnetWebRequest;
import com.futurewei.alcor.web.entity.subnet.SubnetWebRequestJson;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.ArrayList;
import java.util.List;

public class SubnetManagementUtil {

    private static SubnetService subnetService = new SubnetServiceImp(new RestTemplateBuilder());

    public static boolean checkSubnetRequestResourceIsValid(SubnetWebRequestJson resource) {
        if (resource == null || resource.getSubnet() == null) {
            return false;
        }

        SubnetWebRequest subnet = resource.getSubnet();

        // network_id
        String networkId = subnet.getVpcId();
        if (networkId == null) {
            return false;
        }

        // cidr
        String cidr = subnet.getCidr();
        if (cidr == null) {
            return false;
        }

        // ip_version
        Integer ipVersion = subnet.getIpVersion();
        if (ipVersion != null && ipVersion != 4 && ipVersion != 6) {
            return false;
        }

        // ipv6_address_mode
        String ipv6AddressMode = subnet.getIpv6AddressMode();
        if (!(ipv6AddressMode == null || Ipv6AddressModeEnum.SLAAC.getMode().equals(ipv6AddressMode)
                || Ipv6AddressModeEnum.STATEFUL.getMode().equals(ipv6AddressMode)
                || Ipv6AddressModeEnum.STATELESS.getMode().equals(ipv6AddressMode))) {
            return false;
        }

        // ipv6_ra_mode
        String ipv6RaMode = subnet.getIpv6AddressMode();
        if (!(ipv6RaMode == null || Ipv6RaModeEnum.SLAAC.getMode().equals(ipv6RaMode)
                || Ipv6RaModeEnum.STATEFUL.getMode().equals(ipv6RaMode)
                || Ipv6RaModeEnum.STATELESS.getMode().equals(ipv6RaMode))) {
            return false;
        }

        // tags
        List<String> tags = subnet.getTags();
        if (tags == null) {
            tags = new ArrayList<String>() {
            };
            subnet.setTags(tags);
        }

        // dns_nameservers
        List<String> dnsNameservers = subnet.getDnsNameservers();
        if (dnsNameservers == null) {
            dnsNameservers = new ArrayList<String>() {
            };
            subnet.setDnsNameservers(dnsNameservers);
        }

        // host_routes
        List<HostRoute> hostRoutes = subnet.getHostRoutes();
        if (hostRoutes == null) {
            hostRoutes = new ArrayList<HostRoute>() {
            };
            subnet.setHostRoutes(hostRoutes);
        }

        // service_types
        List<String> serviceType = subnet.getServiceTypes();
        if (serviceType == null) {
            serviceType = new ArrayList<String>() {
            };
            subnet.setServiceTypes(serviceType);
        }

        return true;
    }

    public static String adjustGatewayIpValue(String gatewayIp, String cidr) {
        // gatewayIP is null
        if (gatewayIp == null) {
            return null;
        }

        // gatewayIP is empty
        if (gatewayIp.length() == 0) {
            SubnetUtils utils = new SubnetUtils(cidr);
            String lowIp = utils.getInfo().getLowAddress();
            if (lowIp == null) {
                return null;
            }

            String[] lowIps = lowIp.split("\\.");
            Integer low = Integer.parseInt(lowIps[lowIps.length - 1]) + ConstantsConfig.BaseInterval;
            lowIps[lowIps.length - 1] = String.valueOf(low);
            lowIp = String.join(".", lowIps);
            return lowIp;
        }

        return gatewayIp;

    }

    public static boolean checkGatewayIpInputSupported(String gatewayIp, String cidr) throws ParameterUnexpectedValueException, FallbackException {
        // gatewayIP is null
        if (gatewayIp == null) {
            return true;
        }

        // gatewayIP is empty
        if (gatewayIp.length() == 0) {
            return true;
        }

        // gatewayIP is invalid
        if (!checkIpIsValid(gatewayIp)) {
            return false;
        }

        // gatewayIP format is valid but it used the lowest base ip (xx.xx.xx.0) case and use the highest base ip (xx.xx.xx.255))
        boolean isCidrValid = subnetService.verifyCidrBlock(cidr);
        if (!isCidrValid) {
            throw new FallbackException("cidr is invalid : " + cidr);
        }

        String[] ips = subnetService.cidrToFirstIpAndLastIp(cidr);
        if (ips == null || ips.length != 2) {
            throw new FallbackException("cidr transfer to first/last ip failed");
        }

        if (!checkIpIsInRange(gatewayIp, ips[0], ips[1])) {
            return false;
        }

        return true;
    }

    public static boolean checkGatewayIpIsInAllocatedRange(String gatewayIp, String cidr) {

        if (gatewayIp == null || gatewayIp.length() == 0) {
            return false;
        }

        String[] ips = subnetService.cidrToFirstIpAndLastIp(cidr);
        long gatewayIpNum = getIpNum(gatewayIp);
        long firstIpNum = getIpNum(ips[0]);
        long lastIpNum = getIpNum(ips[1]);

        if (gatewayIpNum < firstIpNum || gatewayIpNum > lastIpNum) {
            return false;
        }

        return true;
    }

    public static boolean checkIpIsValid(String gatewayIp) {
        if (gatewayIp == null) {
            return false;
        }
        String[] ips = gatewayIp.split("\\.");

        if (ips == null || ips.length != 4) {
            return false;
        }

        for (String segment : ips) {
            char[] chars = segment.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            Integer num = Integer.parseInt(segment);
            if (num < 0 || num > 255) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkIpIsInRange(String gatewayIp, String firstIp, String lastIp) {
        boolean isInnerIp = false;

        long gatewayIpNum = getIpNum(gatewayIp);
        long firstIpNum = getIpNum(firstIp);
        long lastIpNum = getIpNum(lastIp);

        isInnerIp = isInner(gatewayIpNum, firstIpNum - ConstantsConfig.LowIpInterval, lastIpNum + ConstantsConfig.HighIpInterval);

        return isInnerIp;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);
        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
        return ipNum;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    public static boolean IsCidrWithin(String cidr1, String cidr2) {
        if (cidr2 == null || cidr2.length() == 0) {
            return false;
        }

        if (cidr1 == null || cidr1.length() == 0) {
            return true;
        }

        String[] ips1 = subnetService.cidrToFirstIpAndLastIp(cidr1);
        long firstIp1Num = getIpNum(ips1[0]);
        long lastIp1Num = getIpNum(ips1[1]);

        String[] ips2 = subnetService.cidrToFirstIpAndLastIp(cidr2);
        long firstIp2Num = getIpNum(ips2[0]);
        long lastIp2Num = getIpNum(ips2[1]);

        if (firstIp2Num <= firstIp1Num && lastIp1Num <= lastIp2Num) {
            return true;
        }
        return false;
    }

    public static boolean IsCidrOverlap(String cidr1, String cidr2) {
        if (cidr1 == null || cidr2 == null || cidr1.length() == 0 || cidr2.length() == 0) {
            return false;
        }
        String[] ips1 = subnetService.cidrToFirstIpAndLastIp(cidr1);
        long firstIp1Num = getIpNum(ips1[0]);
        long lastIp1Num = getIpNum(ips1[1]);

        String[] ips2 = subnetService.cidrToFirstIpAndLastIp(cidr2);
        long firstIp2Num = getIpNum(ips2[0]);
        long lastIp2Num = getIpNum(ips2[1]);

        if (lastIp1Num < firstIp2Num || lastIp2Num < firstIp1Num) {
            return false;
        }


        return true;
    }
}
