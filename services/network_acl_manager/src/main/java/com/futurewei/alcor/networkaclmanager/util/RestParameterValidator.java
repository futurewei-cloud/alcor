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

package com.futurewei.alcor.networkaclmanager.util;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.networkaclmanager.exception.*;
import com.futurewei.alcor.web.entity.networkacl.*;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestParameterValidator {
    public static void checkProjectId(String projectId) throws ProjectIdRequired {
        if (StringUtils.isEmpty(projectId)) {
            throw new ProjectIdRequired();
        }
    }

    public static void checkNetworkAcl(NetworkAclEntity networkAclEntity) throws Exception {
        if (networkAclEntity == null) {
            throw new NetworkAclEntityIsNull();
        }

        if (StringUtils.isEmpty(networkAclEntity.getVpcId())) {
            throw new VpcIdRequired();
        }

        List<String> subnetIds = networkAclEntity.getAssociatedSubnets();
        if (subnetIds != null) {
            for (String subnetId: subnetIds) {
                if (StringUtils.isEmpty(subnetId)) {
                    throw new SubnetIdInvalid();
                }
            }
        }
    }

    public static void checkNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        if (networkAclRuleEntity == null) {
            throw new NetworkAclRuleEntityIsNull();
        }

        Integer number = networkAclRuleEntity.getNumber();
        if (number == null || number < 0 || number >= NetworkAclRuleEntity.NUMBER_MAX_VALUE) {
            throw new NetworkAclRuleNumberInvalid();
        }

        if (StringUtils.isEmpty(networkAclRuleEntity.getNetworkAclId())) {
            throw new NetworkAclIdRequired();
        }

        String protocol = networkAclRuleEntity.getProtocol();
        if (protocol != null) {
            List<String> protocols = Arrays.asList(Protocol.values())
                    .stream()
                    .map(Protocol::getProtocol)
                    .collect(Collectors.toList());

            if (!protocols.contains(protocol)) {
                throw new ProtocolInvalid();
            }

            if (Protocol.ICMP.getProtocol().equals(protocol)
                    ||Protocol.ICMPV6.getProtocol().equals(protocol)) {
                Integer icmpType = networkAclRuleEntity.getIcmpType();
                Integer icmpCode = networkAclRuleEntity.getIcmpCode();

                if (icmpType == null || icmpCode == null) {
                    throw new IcmpTypeOrCodeInvalid();
                }

                if (!(0 <= icmpType && icmpType <= 255)) {
                    throw new IcmpTypeInvalid();
                }

                if (!(0 <= icmpCode && icmpCode <= 255)) {
                    throw new IcmpCodeInvalid();
                }
            } else {
                Integer portRangeMax = networkAclRuleEntity.getPortRangeMax();
                Integer portRangeMin = networkAclRuleEntity.getPortRangeMin();

                if (portRangeMax == null || portRangeMin == null) {
                    throw new PortRangeInvalid();
                }

                if (!(portRangeMax > 0 && portRangeMax < 65536)) {
                    throw new PortRangeInvalid();
                }

                if (!(portRangeMin > 0 && portRangeMin < 65536)) {
                    throw new PortRangeInvalid();
                }

                if (portRangeMin > portRangeMax) {
                    throw new PortRangeInvalid();
                }
            }
        }

        if (networkAclRuleEntity.getAction() != null) {
            List<String> actions = Arrays.asList(Action.values())
                    .stream()
                    .map(Action::getAction)
                    .collect(Collectors.toList());
            if (!actions.contains(networkAclRuleEntity.getAction())) {
                throw new ActionInvalid();
            }
        }

        if (networkAclRuleEntity.getDirection() != null) {
            List<String> directions = Arrays.asList(Direction.values())
                    .stream()
                    .map(Direction::getDirection)
                    .collect(Collectors.toList());
            if (!directions.contains(networkAclRuleEntity.getDirection())) {
                throw new DirectionInvalid();
            }
        }

        String etherType = networkAclRuleEntity.getEtherType();
        if (etherType != null) {
            List<String> etherTypes = Arrays.asList(EtherType.values())
                    .stream()
                    .map(EtherType::getEtherType)
                    .collect(Collectors.toList());
            if (!etherTypes.contains(etherType)) {
                throw new EtherTypeInvalid();
            }

            if (Protocol.ICMPV6.getProtocol().equals(protocol)) {
                if (EtherType.IPV4.getEtherType().equals(etherType)) {
                    throw new ProtocolEtherTypeConflict();
                }
            }
        }

        String ipPrefix = networkAclRuleEntity.getIpPrefix();
        if (ipPrefix != null) {
            boolean ipv4Prefix = Ipv4AddrUtil.ipv4PrefixCheck(ipPrefix);
            boolean ipv6Prefix = Ipv6AddrUtil.ipv6PrefixCheck(ipPrefix);
            if (!ipv4Prefix && !ipv6Prefix) {
                throw new IpPrefixInvalid();
            }

            if ((EtherType.IPV4.getEtherType().equals(etherType) && !ipv4Prefix)
                    || (EtherType.IPV6.getEtherType().equals(etherType) && !ipv6Prefix)) {
                if (!ipv4Prefix) {
                    throw new EtherTypeIpPrefixConflict();
                }
            }
        }
    }
}
