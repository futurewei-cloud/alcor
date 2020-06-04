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

package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.dataplane.service.impl.OVSGoalStateServiceImpl;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.web.entity.gsinfo.GoalStateNBJson;
import com.futurewei.alcor.web.entity.gsinfo.HostInfoNB;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.*;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;


public class GoalStateUtil {

    public static Map<String, Goalstate.GoalState> transformNorthToSouth(GoalStateNBJson gs) {
        com.futurewei.alcor.web.entity.port.PortEntity[] portStates1 =
                gs.getPortStates();
        HostInfoNB[] hostInfoNorths
                = gs.getHostInfoNBs();
        com.futurewei.alcor.web.entity.subnet.SubnetEntity[] subnets =
                gs.getSubnets();
        com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcs = gs.getVpcs();

        boolean portFlag, subnetFlag, vpcFlag;
        if (portStates1 != null && portStates1.length > 0) portFlag = true;
        else if (subnets != null && subnets.length > 0) subnetFlag = true;
        else if (vpcs != null && vpcs.length > 0) vpcFlag = true;
        //TODO need to add subnet and vpc part when logic is clear
        Map<String, Set<com.futurewei.alcor.web.entity.port.PortEntity>> northMap = new HashMap<String, Set<com.futurewei.alcor.web.entity.port.PortEntity>>();
        Map<String, Set<com.futurewei.alcor.web.entity.subnet.SubnetEntity>> subnetMap = new HashMap<>();
        Map<String, Set<com.futurewei.alcor.web.entity.vpc.VpcEntity>> vpcMap = new HashMap<>();
        if (portStates1.length != hostInfoNorths.length) return null;
        int counter = 0;
        for (HostInfoNB h : hostInfoNorths) {
            String ip = h.getHostIp();
            if (!northMap.containsKey(ip)) {
                Set<com.futurewei.alcor.web.entity.port.PortEntity> portStates = new HashSet<>();
                portStates.add(portStates1[counter]);
                counter++;
                northMap.put(ip, portStates);
            } else {
                Set<com.futurewei.alcor.web.entity.port.PortEntity> portStates = northMap.get(ip);
                portStates.add(portStates1[counter]);
                counter++;
                northMap.put(ip, portStates);
            }

        }
        List<PortState> portStateArrayList = new ArrayList<>();
        Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
        //TODO would opt this part when perf needed
        northMap.entrySet().stream().forEach(f -> {
            final Set<com.futurewei.alcor.web.entity.port.PortEntity> set =
                    f.getValue();
            final Random random = new Random();
            set.stream().forEach(e -> {

                final Port.PortConfiguration portConfiguration =
                        Port.PortConfiguration.newBuilder().setId(e.getId()).build();
                final PortState portState1 =
                        PortState.newBuilder().setConfiguration(portConfiguration).build();
                PortState portState = AssignVipMacToPort(portState1,
                        random.nextInt(Integer.MAX_VALUE));
                portStateArrayList.add(portState);
//TODO would add fields transformation 1 by 1
//                    .setFixedIps(e.getFixedIps()) // we need an array
            });
            final Goalstate.GoalState goalState =
                    Goalstate.GoalState.newBuilder().addAllPortStates(portStateArrayList).build();
            goalStateHashMap.put(f.getKey(), goalState);


        });


        return goalStateHashMap;
    }

    public static List<Goalstateprovisioner.GoalStateOperationReply> talkToACA(Map<String, Goalstate.GoalState> gss, boolean isFast) {
        //if Config.isOVS
        GoalStateService goalStateService = new OVSGoalStateServiceImpl();

        return gss.entrySet().parallelStream().map(e -> {
            goalStateService.setIp(e.getKey());
            goalStateService.setGoalState(e.getValue());
            goalStateService.setFastPath(isFast);
            return goalStateService.SendGoalStateToHosts();
        }).collect(Collectors.toList());
    }

    public static PortState AssignVipMacToPort(PortState portState,
                                               int epIndex) {

        Port.PortConfiguration.Builder conf =
                portState.getConfiguration().newBuilder();
        conf.setMacAddress(GenerateMacAddress(epIndex));
        String[] vpcIps = new String[]{GenerateIpAddress(epIndex)};
        try {

            List<FixedIp> fixedIps = convertToFixedIps(vpcIps,
                    portState.getConfiguration().getFixedIps(0).getSubnetId());
            for (FixedIp fip : fixedIps) {
                conf.addFixedIps(fip);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        PortState.Builder builder = portState.newBuilderForType();
        builder.setConfiguration(conf);
        return builder.build();
    }

    public static List<FixedIp> convertToFixedIps(String[] vpcIps,
                                                  String subnetId) throws InvalidProtocolBufferException {

        List<FixedIp> fixedIps = new ArrayList<>();
        if (vpcIps != null) {
            for (String vpcIp : vpcIps) {
                FixedIp.Builder builder = FixedIp.newBuilder();
                FixedIp build =
                        builder.setSubnetId(subnetId).setIpAddress(vpcIp).build();
                fixedIps.add(build);
            }
        }

        return fixedIps;
    }

    private static String GenerateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenerateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }
}
