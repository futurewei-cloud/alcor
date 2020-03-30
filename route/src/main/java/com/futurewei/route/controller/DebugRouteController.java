package com.futurewei.route.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.repo.Transaction;
import com.futurewei.route.dao.RouteRepository;
import com.futurewei.route.entity.RouteState;
import com.futurewei.route.entity.RouteState;
import com.futurewei.route.entity.RouteStateJson;
import com.futurewei.route.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DebugRouteController {
    @Autowired(required = false)
    private RouteRepository routeRepository;

    @RequestMapping(
            method = GET,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public RouteStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        RouteState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.routeRepository.findItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (vpcState == null) {
            //TODO: REST error code
            return new RouteStateJson();
        }

        return new RouteStateJson(vpcState);
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpcs")
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = routeRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpccount")
    public Map getVpcCount() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = routeRepository.findAllItems();
        result.put("Count", dataItems.size());

        return result;
    }

    @RequestMapping(
            method = POST,
            value = {"/debug/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public RouteStateJson createVpcState(@PathVariable String projectid, @RequestBody RouteStateJson resource) throws Exception {
        RouteState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            RouteState inVpcState = resource.getRoute();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);

            Transaction transaction = this.routeRepository.getCache().getTransaction();
            transaction.start();

            this.routeRepository.addItem(inVpcState);
            vpcState = this.routeRepository.findItem(inVpcState.getId());

            transaction.commit();

            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new RouteStateJson(vpcState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public RouteStateJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody RouteStateJson resource) throws Exception {

        RouteState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            RouteState inVpcState = resource.getRoute();
            RestPreconditionsUtil.verifyResourceNotNull(inVpcState);
            RestPreconditionsUtil.populateResourceProjectId(inVpcState, projectid);
            RestPreconditionsUtil.populateResourceVpcId(inVpcState, vpcid);

            vpcState = this.routeRepository.findItem(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.routeRepository.addItem(inVpcState);

            vpcState = this.routeRepository.findItem(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new RouteStateJson(vpcState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public ResponseId deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        RouteState vpcState = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            vpcState = this.routeRepository.findItem(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            routeRepository.deleteItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }
}
