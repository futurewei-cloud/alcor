package com.futurewei.alioth.controller.web;

import java.util.Map;
import java.util.stream.Collectors;

import com.futurewei.alioth.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alioth.controller.exception.*;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class VpcController {

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpc/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"})
    public VpcState getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
        }catch (ParameterNullOrEmptyException e){
            //TODO: REST error code
            throw new Exception(e);
        }

        if(vpcState == null){
            //TODO: REST error code
            return new VpcState();
        }

        return vpcState;
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/vpc", "/v4/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcState createVpcState(@PathVariable String projectid, @RequestBody VpcState resource) throws Exception {
        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceNotNull(resource);
            RestPreconditions.populateResourceProjectId(resource, projectid);

            this.vpcRedisRepository.addItem(resource);

            //TODO: Algorithm to determine the hosts
//            HostInfo trHost1 = new HostInfo("tr_hostid_1", "host1", new byte[]{10,0,0,1});
//            HostInfo trHost2 = new HostInfo("tr_hostid_2", "host1", new byte[]{10,0,0,2});
//
//            Goalstate.GoalState goalstate = GoalStateUtil.CreateGoalState(
//                    Common.OperationType.CREATE,
//                    resource,
//                    trHost1.getHostIpAddress(),
//                    trHost2.getHostIpAddress());

//            MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
//            String topic = MessageClient.getGoalStateTopic(trHost1.getId());
//            client.runProducer(topic, goalstate);
        }
        catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }
        catch (ResourceNullException e){
            throw new Exception(e);
        }

        return new VpcState(resource);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/vpc/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"})
    public VpcState updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcState resource) throws Exception {

        VpcState vpcState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.populateResourceProjectId(resource, projectid);
            RestPreconditions.populateResourceVpcId(resource, vpcid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
            if(vpcState == null){
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcRedisRepository.addItem(resource);

            vpcState = this.vpcRedisRepository.findItem(vpcid);

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }

        return vpcState;
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpc/{vpcid}", "/v4/{projectid}/vpcs/{vpcid}"} )
    public void deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
            if (vpcState == null) {
                return;
            }

            vpcRedisRepository.deleteItem(vpcid);
        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs")
    public Map getVpcStatesByProjectId(@PathVariable String projectid) throws Exception {
        Map<String, VpcState> vpcStates = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcStates = this.vpcRedisRepository.findAllItems();
            vpcStates = vpcStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state-> state.getValue()));

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ResourceNotFoundException e){
            throw new Exception(e);
        }

        return vpcStates;
    }
}