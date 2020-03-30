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

package com.futurewei.vpcmanager.controller;

import com.futurewei.vpcmanager.dao.VpcRedisRepository;
import com.futurewei.common.exception.ParameterNullOrEmptyException;
import com.futurewei.common.exception.ResourceNotFoundException;
import com.futurewei.common.exception.ResourceNullException;
import com.futurewei.common.exception.ResourcePersistenceException;
import com.futurewei.common.entity.ResponseId;
import com.futurewei.vpcmanager.entity.RouteWebJson;
import com.futurewei.vpcmanager.entity.RouteWebObject;
import com.futurewei.vpcmanager.entity.VpcState;
import com.futurewei.vpcmanager.entity.VpcStateJson;
import com.futurewei.vpcmanager.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class VpcController {

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(
            method = GET,
            value = {"/rule/{projectid}/vpcs/{vpcid}"})
    public String getRuleByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

//        VpcState vpcState = null;
//
//        try {
//            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
//            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
//            RestPreconditionsUtil.verifyResourceFound(projectid);
//
//            vpcState = this.vpcRedisRepository.findItem(vpcid);
//        } catch (ParameterNullOrEmptyException e) {
//            //TODO: REST error code
//            throw new Exception(e);
//        }
//
//        if (vpcState == null) {
//            //TODO: REST error code
//            return "Not find VPC by vpcId";
//        }

        String url = "http://192.168.137.1:8081/route/rule/" + vpcid; // for docker test
        //String url = "http://192.168.1.17:30003/route/rule/" + vpcid; // for kubernetes test
        return this.restTemplate.getForObject(url, String.class);

    }

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (vpcState == null) {
            //TODO: REST error code
            return new VpcStateJson();
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpcs", "/v4/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcStateJson createVpcState(@PathVariable String projectid, @RequestBody VpcStateJson resource) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);

            this.vpcRedisRepository.addItem(inVpcState);

            vpcState = this.vpcRedisRepository.findItem(inVpcState.getId());
            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }

            //String routeManagerServiceUrl = "http://192.168.1.17:30003/vpcs/" + vpcState.getId() + "/routes"; // for kubernetes test
            String routeManagerServiceUrl = "http://192.168.137.1:8081/vpcs/" + vpcState.getId() + "/routes"; // for docker test
            HttpEntity<VpcStateJson> request = new HttpEntity<>(new VpcStateJson(vpcState));
            RouteWebJson response = restTemplate.postForObject(routeManagerServiceUrl, request, RouteWebJson.class);

            // add RouteWebObject
            if (response != null) {
                List<RouteWebObject> routeWebObjectList = vpcState.getRouteWebObjectList();
                if (routeWebObjectList == null) {
                    routeWebObjectList = new ArrayList<>();
                }
                routeWebObjectList.add(response.getRoute());
                vpcState.setRouteWebObjectList(routeWebObjectList);
            }
            this.vpcRedisRepository.addItem(inVpcState);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }
        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpcs/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcStateJson resource) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inVpcState, vpcid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcRedisRepository.addItem(inVpcState);

            vpcState = this.vpcRedisRepository.findItem(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"})
    public ResponseId deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcRedisRepository.deleteItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs")
    public Map getVpcStatesByProjectId(@PathVariable String projectid) throws Exception {
        Map<String, VpcState> vpcStates = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcStates = this.vpcRedisRepository.findAllItems();
            vpcStates = vpcStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return vpcStates;
    }
}