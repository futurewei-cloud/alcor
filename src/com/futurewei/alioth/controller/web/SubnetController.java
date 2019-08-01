package com.futurewei.alioth.controller.web;

import com.futurewei.alioth.controller.cache.repo.SubnetRedisRepository;
import com.futurewei.alioth.controller.exception.ResourceNullException;
import com.futurewei.alioth.controller.exception.ParameterNullOrEmptyException;
import com.futurewei.alioth.controller.model.*;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class SubnetController {

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/subnet/{subnetId}", "v4/{projectid}/subnets/{subnetId}"})
    public SubnetState getSubnetStateByVpcId(@PathVariable String projectid, @PathVariable String subnetId) throws Exception {

        SubnetState subnetState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetId);
            RestPreconditions.verifyResourceFound(projectid);

            subnetState = this.subnetRedisRepository.findItem(subnetId);
        }catch (ParameterNullOrEmptyException e){
            //TODO: REST error code
            throw new Exception(e);
        }

        if(subnetState == null){
            //TODO: REST error code
            return new SubnetState();
        }

        return subnetState;
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/subnet", "v4/{projectid}/subnets"})
    @ResponseStatus(HttpStatus.CREATED)
    public SubnetState createSubnetState(@RequestBody SubnetState resource) throws Exception {
        try{
            RestPreconditions.verifyResourceNotNull(resource);

            this.subnetRedisRepository.addItem(resource);

            //TODO: Algorithm to determine the hosts
//            HostInfo host1 = new HostInfo("hostid_1", "host1", new byte[]{10,0,0,1});
//            HostInfo host2 = new HostInfo("hostid_1", "host1", new byte[]{10,0,0,2});
//
//            Goalstate.GoalState goalstate = GoalStateUtil.CreateGoalState(
//                    Common.OperationType.CREATE,
//                    resource,
//                    host1.getIpAddress(),
//                    host2.getIpAddress());
//
//            MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
//            String topic = MessageClient.getGoalStateTopic(host1.getId());
//            client.runProducer(topic, goalstate);
        }
        catch (ResourceNullException e){
            throw new Exception(e);
        }

        return new SubnetState(resource);
    }

    @RequestMapping(
            method = GET,
            value = "/project/all/subnets")
    public Map getAllSubnetStates() {
        return this.subnetRedisRepository.findAllItems();
    }
}
