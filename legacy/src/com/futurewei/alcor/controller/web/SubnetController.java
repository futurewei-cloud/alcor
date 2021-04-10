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


package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.db.repo.SubnetRedisRepository;
import com.futurewei.alcor.controller.db.repo.VpcRedisRepository;
import com.futurewei.alcor.controller.exception.*;
import com.futurewei.alcor.controller.model.ResponseId;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.SubnetStateJson;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.web.util.ControllerUtil;
import com.futurewei.alcor.controller.web.util.RestPreconditions;
import com.futurewei.alcor.controller.app.onebox.*;
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
    public SubnetStateJson getSubnetStateById(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

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
            RestPreconditions.verifyResourceFound(inSubnetState.getVpcId());
            RestPreconditions.populateResourceProjectId(inSubnetState, projectid);

            this.subnetRedisRepository.addItem(inSubnetState);

            subnetState = this.subnetRedisRepository.findItem(inSubnetState.getId());
            if (subnetState == null) {
                throw new ResourcePersistenceException();
            }

            VpcState vpcState = this.vpcRedisRepository.findItem(inSubnetState.getVpcId());
            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }

            if (OneBoxConfig.IS_K8S) {
                ControllerUtil.CreateSubnet(subnetState, vpcState);
            } else if (OneBoxConfig.IS_Onebox) {
                OneBoxUtil.CreateSubnet(subnetState);
            }
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

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if (subnetState == null) {
                return new ResponseId();
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

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
