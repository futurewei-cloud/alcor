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
package com.futurewei.alcor.networkaclmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.networkacl.*;

import java.util.*;

public class NetworkAclRuleBuilder {
    public static NetworkAclRuleEntity buildNetworkAclRuleEntity1() {
        NetworkAclRuleEntity networkAclRuleEntity = new NetworkAclRuleEntity();
        networkAclRuleEntity.setId(UnitTestConfig.networkAclRuleId1);
        networkAclRuleEntity.setName(UnitTestConfig.networkAclRuleName1);
        networkAclRuleEntity.setNetworkAclId(UnitTestConfig.networkAclId1);
        networkAclRuleEntity.setIpPrefix(UnitTestConfig.ipv4Prefix1);
        networkAclRuleEntity.setPortRangeMin(UnitTestConfig.portRangeMin1);
        networkAclRuleEntity.setPortRangeMax(UnitTestConfig.portRangeMax1);
        networkAclRuleEntity.setIcmpType(UnitTestConfig.icmpType1);
        networkAclRuleEntity.setIcmpCode(UnitTestConfig.icmpCode1);
        networkAclRuleEntity.setDirection(UnitTestConfig.directionIngress);
        networkAclRuleEntity.setAction(UnitTestConfig.actionDeny);
        networkAclRuleEntity.setNumber(UnitTestConfig.number1);
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeIpv4);
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolTcp);

        return networkAclRuleEntity;
    }

    public static NetworkAclRuleEntity buildNetworkAclRuleEntity2() {
        NetworkAclRuleEntity networkAclRuleEntity = new NetworkAclRuleEntity();
        networkAclRuleEntity.setId(UnitTestConfig.networkAclRuleId2);
        networkAclRuleEntity.setName(UnitTestConfig.networkAclRuleName2);
        networkAclRuleEntity.setNetworkAclId(UnitTestConfig.networkAclId1);
        networkAclRuleEntity.setIpPrefix(UnitTestConfig.ipv4Prefix2);
        networkAclRuleEntity.setPortRangeMin(UnitTestConfig.portRangeMin2);
        networkAclRuleEntity.setPortRangeMax(UnitTestConfig.portRangeMax2);
        networkAclRuleEntity.setIcmpType(UnitTestConfig.icmpType2);
        networkAclRuleEntity.setIcmpCode(UnitTestConfig.icmpCode2);
        networkAclRuleEntity.setDirection(UnitTestConfig.directionEgress);
        networkAclRuleEntity.setAction(UnitTestConfig.actionAllow);
        networkAclRuleEntity.setNumber(UnitTestConfig.number2);
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeIpv4);
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolTcp);

        return networkAclRuleEntity;
    }

    public static List<NetworkAclRuleEntity> buildDefaultNetworkAclRules() {
        List<NetworkAclRuleEntity> networkAclRuleEntities = new ArrayList<>();

        List<String> ipPrefixes = Arrays.asList(NetworkAclRuleEntity.DEFAULT_IPV4_PREFIX,
                NetworkAclRuleEntity.DEFAULT_IPV6_PREFIX);

        for (String ipPrefix: ipPrefixes) {
            List<Direction> directions = Arrays.asList(Direction.INGRESS,
                    Direction.EGRESS);
            for (Direction direction : directions) {
                NetworkAclRuleEntity networkAclRuleEntity = new NetworkAclRuleEntity();
                networkAclRuleEntity.setId(UUID.randomUUID().toString());
                networkAclRuleEntity.setNumber(NetworkAclRuleEntity.NUMBER_MAX_VALUE);
                networkAclRuleEntity.setIpPrefix(ipPrefix);
                networkAclRuleEntity.setProtocol(Protocol.ALL.getProtocol());
                networkAclRuleEntity.setDirection(direction.getDirection());
                networkAclRuleEntity.setAction(Action.DENY.getAction());

                networkAclRuleEntities.add(networkAclRuleEntity);
            }
        }

        return networkAclRuleEntities;
    }

    public static List<NetworkAclRuleEntity> buildNetworkAclRuleEntities() {
        NetworkAclRuleEntity networkAclRuleEntity1 = buildNetworkAclRuleEntity1();
        NetworkAclRuleEntity networkAclRuleEntity2 = buildNetworkAclRuleEntity2();

        List<NetworkAclRuleEntity> networkAclRuleEntities = new ArrayList<>();
        networkAclRuleEntities.add(networkAclRuleEntity1);
        networkAclRuleEntities.add(networkAclRuleEntity2);

        return networkAclRuleEntities;
    }

    public static NetworkAclRuleWebJson buildNetworkAclRuleWebJson() {
        return new NetworkAclRuleWebJson(buildNetworkAclRuleEntity1());
    }

    public static NetworkAclRuleWebJson buildNetworkAclWebJson(String id, String networkAclId, String name,
           String ipPrefix, Integer portRangeMin, Integer portRangeMax, Integer icmpType, Integer icmpCode,
           String direction, String action, Integer number,  String etherType, String protocol) {
        return new NetworkAclRuleWebJson(buildNetworkAclRuleEntity1(id, networkAclId, name, ipPrefix,
                portRangeMin, portRangeMax, icmpType, icmpCode, direction, action, number, etherType, protocol));
    }

    public static String buildNetworkAclRuleWebJsonString() throws Exception {
        NetworkAclRuleWebJson networkAclRuleWebJson = new NetworkAclRuleWebJson(buildNetworkAclRuleEntity1());

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclRuleWebJson);
    }

    public static String buildNetworkAclRuleBulkWebJsonString() throws Exception {
        List<NetworkAclRuleEntity> networkAclRuleEntities = buildNetworkAclRuleEntities();
        NetworkAclRuleBulkWebJson networkAclRuleBulkWebJson =
                new NetworkAclRuleBulkWebJson(networkAclRuleEntities);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclRuleBulkWebJson);
    }

    public static String buildNetworkAclRuleWebJsonString(NetworkAclRuleEntity networkAclRuleEntity) throws Exception {
        NetworkAclRuleWebJson networkAclRuleWebJson = new NetworkAclRuleWebJson(networkAclRuleEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclRuleWebJson);
    }

    public static NetworkAclRuleEntity buildNetworkAclRuleEntity1(String id, String networkAclId, String name,
                                                                  String ipPrefix, Integer portRangeMin, Integer portRangeMax, Integer icmpType, Integer icmpCode,
                                                                  String direction, String action, Integer number, String etherType, String protocol) {
        NetworkAclRuleEntity networkAclRuleEntity = new NetworkAclRuleEntity();
        networkAclRuleEntity.setId(id);
        networkAclRuleEntity.setName(name);
        networkAclRuleEntity.setNetworkAclId(networkAclId);
        networkAclRuleEntity.setIpPrefix(ipPrefix);
        networkAclRuleEntity.setPortRangeMin(portRangeMin);
        networkAclRuleEntity.setPortRangeMax(portRangeMax);
        networkAclRuleEntity.setIcmpType(icmpType);
        networkAclRuleEntity.setIcmpCode(icmpCode);
        networkAclRuleEntity.setDirection(direction);
        networkAclRuleEntity.setAction(action);
        networkAclRuleEntity.setNumber(number);
        networkAclRuleEntity.setEtherType(etherType);
        networkAclRuleEntity.setProtocol(protocol);

        return networkAclRuleEntity;
    }

    public static String buildNetworkAclRuleWebJsonString(String id, String networkAclId, String name,
          String ipPrefix, Integer portRangeMin, Integer portRangeMax, Integer icmpType, Integer icmpCode,
          String direction, String action, Integer number,  String etherType, String protocol) throws Exception {
        NetworkAclRuleWebJson networkAclRuleWebJson = new NetworkAclRuleWebJson(
                buildNetworkAclRuleEntity1(UnitTestConfig.networkAclRuleId1,
                        UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclRuleName1,
                        UnitTestConfig.ipv4Prefix1,
                        UnitTestConfig.portRangeMin1,
                        UnitTestConfig.portRangeMax1,
                        UnitTestConfig.icmpType1,
                        UnitTestConfig.icmpCode1,
                        UnitTestConfig.directionIngress,
                        UnitTestConfig.actionDeny,
                        UnitTestConfig.number1,
                        UnitTestConfig.etherTypeIpv4,
                        UnitTestConfig.protocolTcp));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclRuleWebJson);
    }

}
