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

package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.vpcmanager.dao.VpcRepository;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.vpcmanager.entity.VpcState;
import com.futurewei.alcor.vpcmanager.entity.VpcStateJson;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DebugVpcController {
    @Autowired(required = false)
    private VpcDatabaseService vpcDatabaseService;

    @RequestMapping(
            method = GET,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);
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
            method = GET,
            value = "/debug/project/all/vpcs")
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcDatabaseService.getAllVpcs();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpccount")
    public Map getVpcCount() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcDatabaseService.getAllVpcs();
        result.put("Count", dataItems.size());

        return result;
    }

    @RequestMapping(
            method = POST,
            value = {"/debug/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcStateJson createVpcState(@PathVariable String projectid, @RequestBody VpcStateJson resource) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);

            Transaction transaction = this.vpcDatabaseService.getCache().getTransaction();
            transaction.start();

            this.vpcDatabaseService.addVpc(inVpcState);
            vpcState = this.vpcDatabaseService.getByVpcId(inVpcState.getId());

            transaction.commit();

            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcStateJson resource) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inVpcState, vpcid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcDatabaseService.addVpc(inVpcState);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public ResponseId deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcDatabaseService.getByVpcId(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcDatabaseService.deleteVpc(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }
}
