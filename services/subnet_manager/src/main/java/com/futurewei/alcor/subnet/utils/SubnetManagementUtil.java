/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.subnet.utils;

import com.futurewei.alcor.common.enumClass.Ipv6AddressModeEnum;
import com.futurewei.alcor.common.enumClass.Ipv6RaModeEnum;
import com.futurewei.alcor.web.entity.subnet.SubnetRequestWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetWebRequestObject;

public class SubnetManagementUtil {

    public static boolean checkSubnetRequestResourceIsValid(SubnetRequestWebJson resource) {
        if (resource == null) {
            return false;
        }
        SubnetWebRequestObject subnet = resource.getSubnet();

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
                || Ipv6AddressModeEnum.STATELESS.getMode().equals(ipv6AddressMode)) ) {
            return false;
        }

        // ipv6_ra_mode
        String ipv6RaMode = subnet.getIpv6AddressMode();
        if (!(ipv6RaMode == null || Ipv6RaModeEnum.SLAAC.getMode().equals(ipv6RaMode)
                || Ipv6RaModeEnum.STATEFUL.getMode().equals(ipv6RaMode)
                || Ipv6RaModeEnum.STATELESS.getMode().equals(ipv6RaMode)) ) {
            return false;
        }

        return true;
    }

}
