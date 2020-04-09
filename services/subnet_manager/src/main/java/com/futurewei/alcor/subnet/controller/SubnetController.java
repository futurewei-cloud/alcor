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

package com.futurewei.alcor.subnet.controller;

import com.futurewei.alcor.subnet.dao.SubnetRedisRepository;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SubnetController {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Value("${microservices.mac.service.url}")
    private String macUrl;

    @Value("${microservices.ip.service.url}")
    private String ipUrl;

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetId}", "v4/{projectid}/subnets/{subnetId}"})
    public SubnetStateJson getSubnetStateById(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            subnetState = this.subnetRedisRepository.findItem(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (subnetState == null) {
            //TODO: REST error code
            return new SubnetStateJson();
        }

        return new SubnetStateJson(subnetState);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/subnets", "v4/{projectid}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetStateJson createSubnetState(@PathVariable String projectid, @RequestBody SubnetStateJson resource) throws Exception {
        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceFound(inSubnetState.getVpcId());
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectid);

            this.subnetRedisRepository.addItem(inSubnetState);

            subnetState = this.subnetRedisRepository.findItem(inSubnetState.getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            // Verify VPC ID
            String vpcManagerServiceUrl = vpcUrl + projectid + "/vpcs/" + inSubnetState.getVpcId(); // for kubernetes test
            //HttpEntity<SubnetStateJson> vpcRequest = new HttpEntity<>(new SubnetStateJson(subnetState));
            VpcStateJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcStateJson.class);
            if (vpcResponse == null) {
                throw new ResourcePersistenceException();
            }

            //Prepare Route Rule(IPv4/6) for Subnet
            String routeManagerServiceUrl = routeUrl + inSubnetState.getId() + "/routes"; // for kubernetes test
            HttpEntity<VpcStateJson> routeRequest = new HttpEntity<>(new VpcStateJson(vpcResponse.getVpc()));
            RouteWebJson routeResponse = restTemplate.postForObject(routeManagerServiceUrl, routeRequest, RouteWebJson.class);
            if (routeResponse == null) {
                throw new ResourcePersistenceException();
            }

            //Allocate Gateway Mac
//            MacState macState = new MacState();
//            String portId = UUID.randomUUID().toString();
//            macState.setProjectId(projectid);
//            macState.setPortId(portId);
//            macState.setVpcId(inSubnetState.getVpcId());
//
//            HttpEntity<MacStateJson> macRequest = new HttpEntity<>(new MacStateJson(macState));
//            MacStateJson macResponse = restTemplate.postForObject(macUrl, macRequest, MacStateJson.class);
//            if (macResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
//            IPState ipState = new IPState();
//            ipState.setSubnetId(inSubnetState.getId());
//            ipState.setPortId(portId);
//            ipState.setSubnetCidr(inSubnetState.getCidr());
//
//            String ipManagerServiceUrl = ipUrl + inSubnetState.getId() + "/routes"; // for kubernetes test
//            HttpEntity<IPStateJson> ipRequest = new HttpEntity<>(new IPStateJson(ipState));
//            IPStateJson ipResponse = restTemplate.postForObject(ipManagerServiceUrl, ipRequest, IPStateJson.class);
//            if (ipResponse == null) {
//                throw new ResourcePersistenceException();
//            }

            // set up value of properties for subnetState
            //subnetState.setGatewayIp(ipResponse.getIpState().getIp());

        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new SubnetStateJson(subnetState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public SubnetStateJson updateSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid, @RequestBody SubnetStateJson resource) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditionsUtil.verifyResourceNotNull(inSubnetState);
            RestPreconditionsUtil.populateResourceProjectId(inSubnetState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inSubnetState, vpcid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditionsUtil.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            this.subnetRedisRepository.addItem(inSubnetState);
            subnetState = this.subnetRedisRepository.findItem(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new SubnetStateJson(subnetState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}/subnets/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public ResponseId deleteSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(subnetid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                return new ResponseId();
            }

            RestPreconditionsUtil.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditionsUtil.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            subnetRedisRepository.deleteItem(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new ResponseId(subnetid);
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs/{vpcid}/subnets")
    public Map geSubnetStatesByProjectIdAndVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        Map<String, SubnetState> subnetStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);
            RestPreconditionsUtil.verifyResourceFound(vpcid);

            subnetStates = this.subnetRedisRepository.findAllItems();
            subnetStates = subnetStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId())
                            && vpcid.equalsIgnoreCase(state.getValue().getVpcId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return subnetStates;
    }


}
