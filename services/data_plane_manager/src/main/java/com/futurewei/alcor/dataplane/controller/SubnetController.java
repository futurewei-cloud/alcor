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

import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.dataplane.dao.repo.SubnetRepository;
import com.futurewei.alcor.dataplane.dao.repo.VpcRepository;
import com.futurewei.alcor.dataplane.entity.ResponseId;
import com.futurewei.alcor.dataplane.entity.SubnetStateJson;
import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.dataplane.utils.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static com.futurewei.alcor.schema.Vpc.VpcState;
import static org.springframework.web.bind.annotation.RequestMethod.*;
@RestController
public class SubnetController {

    @Autowired
    private VpcRepository vpcRepository;

    @Autowired
    private SubnetRepository subnetRepository;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetId}", "v4/{projectid}/subnets/{subnetId}"})
    public SubnetStateJson getSubnetStateById(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditions.verifyResourceFound(projectid);

            subnetState = this.subnetRepository.findItem(subnetId);
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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceNotNull(resource.getSubnet());

            // TODO: Create a verification framework for all resources
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditions.verifyResourceFound(inSubnetState.getConfiguration().getVpcId());
            RestPreconditions.populateResourceProjectId(inSubnetState, projectid);

            this.subnetRepository.addItem(inSubnetState);

            subnetState = this.subnetRepository.findItem(inSubnetState.getConfiguration().getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            VpcState vpcState = this.vpcRepository.findItem(inSubnetState.getConfiguration().getVpcId());
            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }
            GoalStateUtil.CreateSubnet(subnetState, vpcState);
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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);
            SubnetState inSubnetState = resource.getSubnet();
            RestPreconditions.verifyResourceNotNull(inSubnetState);
            RestPreconditions.populateResourceProjectId(inSubnetState, projectid);
            RestPreconditions.populateResourceVpcId(inSubnetState, vpcid);

            subnetState = this.subnetRepository.findItem(subnetid);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditions.verifyParameterEqual(subnetState.getConfiguration().getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getConfiguration().getVpcId(), vpcid);

            this.subnetRepository.addItem(inSubnetState);
            subnetState = this.subnetRepository.findItem(subnetid);

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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);

            subnetState = this.subnetRepository.findItem(subnetid);
            if (subnetState == null) {
                return new ResponseId();
            }

            RestPreconditions.verifyParameterEqual(subnetState.getConfiguration().getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getConfiguration().getVpcId(), vpcid);

            subnetRepository.deleteItem(subnetid);

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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);
            RestPreconditions.verifyResourceFound(vpcid);

            subnetStates = this.subnetRepository.findAllItems();
            subnetStates = subnetStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getConfiguration().getProjectId())
                            && vpcid.equalsIgnoreCase(state.getValue().getConfiguration().getVpcId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return subnetStates;
    }


}
