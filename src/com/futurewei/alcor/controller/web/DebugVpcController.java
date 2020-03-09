package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.cache.repo.VpcIgniteRepository;
import com.futurewei.alcor.controller.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.controller.exception.ResourceNotFoundException;
import com.futurewei.alcor.controller.exception.ResourceNullException;
import com.futurewei.alcor.controller.exception.ResourcePersistenceException;
import com.futurewei.alcor.controller.model.ResponseId;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.model.VpcStateJson;
import com.futurewei.alcor.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
public class DebugVpcController {
    @Autowired(required = false)
    private VpcIgniteRepository vpcIgniteRepository;

    @RequestMapping(
            method = GET,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcIgniteRepository.findItem(vpcid);
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
            value = {"/debug/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcStateJson createVpcState(@PathVariable String projectid, @RequestBody VpcStateJson resource) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditions.verifyResourceNotNull(inVpcState);
            RestPreconditions.populateResourceProjectId(inVpcState, projectid);

            this.vpcIgniteRepository.addItem(inVpcState);

            vpcState = this.vpcIgniteRepository.findItem(inVpcState.getId());
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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditions.verifyResourceNotNull(inVpcState);
            RestPreconditions.populateResourceProjectId(inVpcState, projectid);
            RestPreconditions.populateResourceVpcId(inVpcState, vpcid);

            vpcState = this.vpcIgniteRepository.findItem(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcIgniteRepository.addItem(inVpcState);

            vpcState = this.vpcIgniteRepository.findItem(vpcid);

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
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcIgniteRepository.findItem(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcIgniteRepository.deleteItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }
}
