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

package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.cache.repo.SubnetRedisRepository;
import com.futurewei.alcor.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alcor.controller.exception.*;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.web.util.RestPreconditions;
import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class SubnetController {

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnets/{subnetId}", "v4/{projectid}/subnets/{subnetId}"})
    public SubnetState getSubnetStateById(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditions.verifyResourceFound(projectid);

            subnetState = this.subnetRedisRepository.findItem(subnetId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (subnetState == null) {
            //TODO: REST error code
            return new SubnetState();
        }

        return subnetState;
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/subnet", "v4/{projectid}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetState createSubnetState(@PathVariable String projectid, @RequestBody SubnetState resource) throws Exception {
        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceNotNull(resource);

            // TODO: Create a verification framework for all resources
            RestPreconditions.verifyResourceFound(resource.getVpcId());
            RestPreconditions.populateResourceProjectId(resource, projectid);

            this.subnetRedisRepository.addItem(resource);

            SubnetState subnetState = this.subnetRedisRepository.findItem(resource.getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            VpcState vpcState = this.vpcRedisRepository.findItem(resource.getVpcId());
            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }

            if (OneBoxConfig.IS_Onebox) {
                OneBoxUtil.CreateSubnet(subnetState);
            }
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new SubnetState(resource);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpc/{vpcid}/subnet/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public SubnetState updateSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid, @RequestBody SubnetState resource) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditions.verifyResourceNotNull(resource);
            RestPreconditions.populateResourceProjectId(resource, projectid);
            RestPreconditions.populateResourceVpcId(resource, vpcid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            this.subnetRedisRepository.addItem(resource);
            subnetState = this.subnetRedisRepository.findItem(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return subnetState;
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpc/{vpcid}/subnet/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public void deleteSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid) throws Exception {

        SubnetState subnetState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                return;
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            subnetRedisRepository.deleteItem(subnetid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpc/{vpcid}/subnets")
    public Map geSubnetStatesByProjectIdAndVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        Map<String, SubnetState> subnetStates = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);
            RestPreconditions.verifyResourceFound(vpcid);

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
