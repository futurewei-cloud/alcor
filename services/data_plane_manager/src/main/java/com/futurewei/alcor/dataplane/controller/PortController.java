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

package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.message.MessageClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.config.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.dataplane.config.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.dataplane.dao.repo.PortRepository;
import com.futurewei.alcor.dataplane.dao.repo.SubnetRepository;
import com.futurewei.alcor.dataplane.dao.repo.VpcRepository;
import com.futurewei.alcor.dataplane.entity.PortStateGroup;
import com.futurewei.alcor.dataplane.entity.PortStateJson;
import com.futurewei.alcor.dataplane.entity.ResponseId;
import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.dataplane.utils.RestPreconditions;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortState;
import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class PortController {

    @Autowired
    private VpcRepository vpcRepository;

    @Autowired
    private SubnetRepository subnetRepository;

    @Autowired
    private PortRepository portRepository;

    @RequestMapping(method = GET, value = {"/project/{projectid}/ports" +
            "/{portId}", "v4/{projectid}/ports/{portId}"})
    public PortStateJson getPortStateById(@PathVariable String projectid,
                                          @PathVariable String portId) throws Exception {

        PortState portState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portId);
            RestPreconditions.verifyResourceFound(projectid);

            portState = this.portRepository.findItem(portId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (portState == null) {
            //TODO: REST error code
            return new PortStateJson();
        }

        return new PortStateJson(portState);
    }

    @RequestMapping(method = POST, value = {"/project/{projectid}/ports", "v4" +
            "/{projectid}/ports"})
    @ResponseStatus(HttpStatus.CREATED)
    public PortStateJson createPortState(@PathVariable String projectid,
                                         @RequestBody PortStateJson resource) throws Exception {

        long T0 = System.nanoTime();
        PortState customerPortState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceFound(projectid);

            // TODO: Create a verification framework for all resources
            PortState portState = resource.getPort();
            RestPreconditions.verifyResourceNotNull(portState);
//            RestPreconditions.verifyResourceFound(portState.getNetworkId());
//            RestPreconditions.verifyResourceNotExists(portState.getId());
            RestPreconditions.populateResourceProjectId(portState, projectid);

            long T1 = System.nanoTime();

            customerPortState = GoalStateUtil.CreatePort(portState);

            this.portRepository.addItem(customerPortState);

        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new PortStateJson(customerPortState);
    }

    @RequestMapping(method = PUT, value = {"/project/{projectid}/ports" +
            "/{portid}", "v4/{projectid}/ports/{portid}"})
    public PortStateJson updateSubnetState(@PathVariable String projectid,
                                           @PathVariable String portid,
                                           @RequestBody PortStateJson resource) throws Exception {

        PortState currentPortState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);

            PortState updatedPortState = resource.getPort();
            RestPreconditions.verifyResourceNotNull(updatedPortState);
            RestPreconditions.verifyResourceFound(updatedPortState.getConfiguration().getNetworkId());
            RestPreconditions.populateResourceProjectId(updatedPortState,
                    projectid);

            currentPortState = this.portRepository.findItem(portid);
            if (currentPortState == null) {
                throw new ResourceNotFoundException("Port not found : " + portid);
            }

            RestPreconditions.verifyParameterEqual(currentPortState.getConfiguration().getProjectId(), projectid);

            this.portRepository.addItem(updatedPortState);
            currentPortState = this.portRepository.findItem(portid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new PortStateJson(currentPortState);
    }

    @RequestMapping(method = DELETE, value = {"/project/{projectid}/ports" +
            "/{portid}", "v4/{projectid}/ports/{portid}"})
    public ResponseId deletePortState(@PathVariable String projectid,
                                      @PathVariable String portid) throws Exception {

        PortState portState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);

            portState = this.portRepository.findItem(portid);
            if (portState == null) {
                return new ResponseId();
            }

            RestPreconditions.verifyParameterEqual(portState.getConfiguration().getProjectId(), projectid);

            portRepository.deleteItem(portid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new ResponseId(portid);
    }

    @RequestMapping(method = GET, value = "/project/{projectid}/subnet" +
            "/{subnetid}/ports")
    public Map gePortStatesByProjectIdAndSubnetId(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {
        Map<String, PortState> portStates = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditions.verifyResourceFound(projectid);
            RestPreconditions.verifyResourceFound(subnetid);

            portStates = this.portRepository.findAllItems();
//            portStates.get(0).getDefaultInstanceForType()
            portStates =
                    portStates.entrySet().stream().filter(state -> projectid.equalsIgnoreCase(state.getValue().getConfiguration().getProjectId()) && subnetid.equalsIgnoreCase(state.getValue().getConfiguration().getNetworkId())).collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return portStates;
    }


    private static PortState GeneretePortState(HostInfo hostInfo, int epIndex) {
        PortState.Builder builder = PortState.newBuilder();
        Port.PortConfiguration.Builder builder1 =
                Port.PortConfiguration.newBuilder();
        builder1.setProjectId(Config.projectId).setVethName(Config.VETH_NAME).setId(epIndex + "_" + hostInfo.getIpAddress()).setName(epIndex + "_" + hostInfo.getIpAddress()).setMacAddress(GenereateMacAddress(epIndex));
        String[] strings = {GenereateIpAddress(epIndex)};
        for (String ip : strings) {
            Port.PortConfiguration.FixedIp.Builder builder3 =
                    Port.PortConfiguration.FixedIp.newBuilder();
            Port.PortConfiguration.FixedIp build =
                    builder3.setIpAddress(ip).setSubnetId(Config.subnetId).build();
            builder1.addFixedIps(build);
        }
        return builder.setConfiguration(builder1).build();
    }

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }

    public static long[] CreatePorts(List<PortState> portStates,
                                     int hostIndex, int epStartIndex,
                                     int epEndIndex) {

        System.out.println("EP host index :" + hostIndex + "; EP start index:" +
                " " + epStartIndex + "; end index: " + epEndIndex);
        long[] recordedTimeStamp = new long[3];
        boolean isFastPath = true; //portStates.get(0).isFastPath();

        SubnetState customerSubnetState = Config.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet =
                Config.transitSwitchHosts;
//        HostInfo[] transitSwitchHostsForSubnet = new HostInfo[1];
//        transitSwitchHostsForSubnet[0] = transitSwitchHostsForSubnet1;
        PortState[] customerPortStates =
                new PortState[epEndIndex - epStartIndex];
        HostInfo epHost = Config.epHosts.get(hostIndex);

        for (int i = 0; i < epEndIndex - epStartIndex; i++) {
            int epIndex = epStartIndex + i;
            PortState customerPortState = GeneretePortState(epHost, epIndex);
            customerPortStates[i] = customerPortState;
        }

        GoalStateProvisionerClient gRpcClientForEpHost =
                new GoalStateProvisionerClient(Config.gRPCServerIp,
                        NodeManager.GRPC_SERVER_PORT);
        MessageClient kafkaClient =
                new MessageClient(new GoalStateMessageConsumerFactory(),
                        new GoalStateMessageProducerFactory());
        String topicForEndpoint =
                Config.HOST_ID_PREFIX + epHost.getIpAddress();

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to EP host, update_endpoint
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsPortState =
                GoalStateUtil.CreateGoalState(Common.OperationType.INFO,
                        customerSubnetState, transitSwitchHostsForSubnet,
                        Common.OperationType.CREATE, customerPortStates,
                        epHost);

        if (isFastPath) {
            System.out.println("Sending " + customerPortStates.length + " " +
                    "ports with fast path");
            System.out.println("Sending: " + gsPortState);
            gRpcClientForEpHost.PushNetworkResourceStates(gsPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsPortState);
        }

        recordedTimeStamp[0] = System.nanoTime();

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to switch hosts in current subnet, update_ep and
        // update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsPortStateForSwitch =
                GoalStateUtil.CreateGoalState(Common.OperationType.INFO,
                        customerSubnetState, transitSwitchHostsForSubnet,
                        Common.OperationType.CREATE_UPDATE_SWITCH,
                        customerPortStates, epHost);

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet) {
            if (isFastPath) {
                System.out.println("Sending " + customerPortStates.length +
                        " ports to transit switch with fast path");
                System.out.println("Sending: " + gsPortStateForSwitch);
                GoalStateProvisionerClient gRpcClientForSwitchHost =
                        new GoalStateProvisionerClient(Config.gRPCServerIp, NodeManager.GRPC_SERVER_PORT);
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch =
                        Config.HOST_ID_PREFIX + switchForSubnet.getIpAddress();
                kafkaClient.runProducer(topicForSwitch, gsPortStateForSwitch);
            }
        }

        recordedTimeStamp[1] = System.nanoTime();

        ////////////////////////////////////////////////////////////////////////////
        // Step 3: Go to EP host, update_agent_md and update_agent_ep
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsFinalizedPortState =
                GoalStateUtil.CreateGoalState(Common.OperationType.INFO,
                        customerSubnetState, transitSwitchHostsForSubnet,
                        Common.OperationType.FINALIZE, customerPortStates,
                        epHost);

        if (isFastPath) {
            System.out.println("Sending " + customerPortStates.length + " " +
                    "with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }

    private static long[][] CreatePortGroup(PortStateGroup portStateGroup) {
        List<PortState> portStates = portStateGroup.getPortStates();
        int portCount = portStates.size();
        int epHostCount = Config.epHosts.size();
        int portCountPerHost = portCount / epHostCount > 0 ?
                portCount / epHostCount : 1;

        long[][] results = new long[epHostCount][];

        ExecutorService executor =
                Executors.newFixedThreadPool(Config.THREADS_LIMIT);
        CompletionService<long[]> goalStateProgrammingService =
                new ExecutorCompletionService<long[]>(executor);

        for (int i = 0; i < epHostCount; i++) {

            if (Config.IS_PARALLEL) {
                final int nodeIndex = i;

                goalStateProgrammingService.submit(new Callable<long[]>() {
                    @Override
                    public long[] call() throws IllegalStateException {
                        String name = Thread.currentThread().getName();
                        System.out.println("Running on thread " + name);

                        return PortController.CreatePorts(portStates,
                                nodeIndex, nodeIndex * portCountPerHost,
                                (nodeIndex + 1) * portCountPerHost);
                    }
                });
            } else {
                long[] times = PortController.CreatePorts(portStates, i,
                        i * portCountPerHost, (i + 1) * portCountPerHost);
                results[i] = times;
            }
        }

        int received = 0;
        boolean errors = false;

        while (Config.IS_PARALLEL && received < epHostCount && !errors) {
            try {
                Future<long[]> resultFuture =
                        goalStateProgrammingService.take();
                long[] result = resultFuture.get();
                results[received] = result;
                received++;
            } catch (Exception e) {
                e.printStackTrace();
                errors = true;
            }
        }

        return results;
    }


    @RequestMapping(method = POST, value = {"/project/{projectid}/portgroup"}
    , consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PortState> createPortStates(@PathVariable String projectid,
                                            @RequestBody PortStateGroup resourceGroup) throws Exception {

        long T0 = System.nanoTime();
        List<PortState> response = new ArrayList<>();

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceFound(projectid);

            List<PortState> portStates = resourceGroup.getPortStates();
            for (PortState state : portStates) {
                this.portRepository.addItem(state);
                response.add(state);
            }
            long T1 = System.nanoTime();

            long[][] elapsedTimes = CreatePortGroup(resourceGroup);
            int hostCount = elapsedTimes.length;

            long averageElapseTime = 0, minElapseTime = Long.MAX_VALUE,
                    maxElapseTime = Long.MIN_VALUE;
            System.out.println("Total number of time sequences:" + hostCount);
            for (int i = 0; i < hostCount; i++) {
                long et = elapsedTimes[i][2] - T0;
                averageElapseTime += et;
                if (et < minElapseTime) minElapseTime = et;
                if (et > maxElapseTime) maxElapseTime = et;
                RestPreconditions.recordRequestTimeStamp(resourceGroup.getPortState(i).getConfiguration().getId(), T0, T1, elapsedTimes[i]);
            }

            Config.TIME_STAMP_WRITER.newLine();
            Config.TIME_STAMP_WRITER.write("," + averageElapseTime / (1000000 * hostCount) + "," + minElapseTime / 1000000 + "," + maxElapseTime / 1000000);
            Config.TIME_STAMP_WRITER.newLine();
            Config.TIME_STAMP_WRITER.write("Average time of " + Config.TOTAL_REQUEST + " requests :" + Config.TOTAL_TIME / Config.TOTAL_REQUEST + " ms");
            if (Config.TIME_STAMP_WRITER != null)
                Config.TIME_STAMP_WRITER.close();

        } catch (Exception e) {
            throw e;
        }

        return response;
    }

}
