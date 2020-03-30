package com.futurewei.vpcmanager.controller;

import com.futurewei.common.repo.Transaction;
import com.futurewei.vpcmanager.dao.VpcRepository;
import com.futurewei.common.exception.*;
import com.futurewei.common.exception.CacheException;
import com.futurewei.common.entity.ResponseId;
import com.futurewei.vpcmanager.entity.VpcState;
import com.futurewei.vpcmanager.entity.VpcStateJson;
import com.futurewei.vpcmanager.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DebugVpcController {
    @Autowired(required = false)
    private VpcRepository vpcRepository;

    @RequestMapping(
            method = GET,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.vpcRepository.findItem(vpcid);
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
        Map dataItems = vpcRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpccount")
    public Map getVpcCount() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcRepository.findAllItems();
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

            Transaction transaction = this.vpcRepository.getCache().getTransaction();
            transaction.start();

            this.vpcRepository.addItem(inVpcState);
            vpcState = this.vpcRepository.findItem(inVpcState.getId());

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

            vpcState = this.vpcRepository.findItem(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcRepository.addItem(inVpcState);

            vpcState = this.vpcRepository.findItem(vpcid);

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

            vpcState = this.vpcRepository.findItem(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcRepository.deleteItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }
}
