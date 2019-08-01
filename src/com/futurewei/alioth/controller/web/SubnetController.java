package com.futurewei.alioth.controller.web;

import com.futurewei.alioth.controller.cache.repo.*;
import com.futurewei.alioth.controller.comm.message.*;
import com.futurewei.alioth.controller.comm.message.MessageClient;
import com.futurewei.alioth.controller.exception.ResourceNullException;
import com.futurewei.alioth.controller.exception.ParameterNullOrEmptyException;
import com.futurewei.alioth.controller.exception.ResourcePersistenceException;
import com.futurewei.alioth.controller.model.*;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
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
    private VpcRedisRepository vpcRedisRepository;

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
    public SubnetState createSubnetState(@PathVariable String projectid, @RequestBody SubnetState resource) throws Exception {
        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceNotNull(resource);

            // TODO: Create a verification framework for all resources
            RestPreconditions.verifyResourceFound(resource.getVpcId());
            RestPreconditions.populateResourceProjectId(resource, projectid);

            this.subnetRedisRepository.addItem(resource);

            //TODO: Algorithm to allocate transit switches and routers
            HostInfo[] transitSwitches = {
                    new HostInfo("ts-1", "transit switch host1", new byte[]{10,0,0,11}),
                    new HostInfo("ts-2", "transit switch host2", new byte[]{10,0,0,12})
            };
            HostInfo[] transitRouters = {
                    new HostInfo("tr-1", "transit router host1", new byte[]{10,0,0,1}),
                    new HostInfo("tr-2", "transit router host2", new byte[]{10,0,0,2})
            };
            MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

            // Generate subnet goal states and send them to all transit routers
            SubnetState subnetState = this.subnetRedisRepository.findItem(resource.getId());
            if(subnetState == null){
                throw new ResourcePersistenceException();
            }
            GoalState subnetGoalState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.CREATE_UPDATE_ROUTER,
                    subnetState,
                    transitSwitches[0].getHostIpAddress(),
                    transitSwitches[1].getHostIpAddress());
            for(HostInfo transitRouter : transitRouters){
                String topic = MessageClient.getGoalStateTopic(transitRouter.getId());
                client.runProducer(topic, subnetGoalState);
            }

            // Generate vpc goal states and send them to all transit switches
            VpcState vpcState = this.vpcRedisRepository.findItem(resource.getVpcId());
            GoalState vpcGoalstate = GoalStateUtil.CreateGoalState(
                    Common.OperationType.CREATE_UPDATE_SWITCH,
                    vpcState,
                    transitRouters[0].getHostIpAddress(),
                    transitRouters[1].getHostIpAddress());
            for(HostInfo transitSwitch : transitSwitches)
            {
                String topic = MessageClient.getGoalStateTopic(transitSwitch.getId());
                client.runProducer(topic, vpcGoalstate);
            }
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
