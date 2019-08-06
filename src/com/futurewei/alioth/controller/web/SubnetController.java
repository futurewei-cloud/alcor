package com.futurewei.alioth.controller.web;

import com.futurewei.alioth.controller.cache.repo.*;
import com.futurewei.alioth.controller.app.DemoConfig;
import com.futurewei.alioth.controller.comm.message.*;
import com.futurewei.alioth.controller.comm.message.MessageClient;
import com.futurewei.alioth.controller.exception.*;
import com.futurewei.alioth.controller.model.*;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
                    new HostInfo(DemoConfig.TRANSIT_SWTICH_1_HOST_ID, "transit switch host1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
                    new HostInfo(DemoConfig.TRANSIT_SWTICH_2_HOST_ID, "transit switch host2", DemoConfig.TRANSIT_SWITCH_2_IP, DemoConfig.TRANSIT_SWITCH_2_MAC)
            };
            HostInfo[] transitRouters = {
                    new HostInfo(DemoConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
                    new HostInfo(DemoConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
            };
            MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

            // Generate subnet goal states and send them to all transit routers
            SubnetState subnetState = this.subnetRedisRepository.findItem(resource.getId());
            if(subnetState == null){
                throw new ResourcePersistenceException();
            }
            GoalState subnetGoalState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.CREATE_UPDATE_ROUTER,
                    new SubnetState[]{subnetState},
                    new HostInfo[][]{transitSwitches});
            for(HostInfo transitRouter : transitRouters){
                String topic = MessageClient.getGoalStateTopic(transitRouter.getId());
                client.runProducer(topic, subnetGoalState);
            }

            // Generate vpc goal states and send them to all transit switches
            VpcState vpcState = this.vpcRedisRepository.findItem(resource.getVpcId());
            GoalState vpcGoalstate = GoalStateUtil.CreateGoalState(
                    Common.OperationType.CREATE_UPDATE_SWITCH,
                    vpcState,
                    transitRouters);
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
            method = PUT,
            value = {"/project/{projectid}/vpc/{vpcid}/subnet/{subnetid}", "v4/{projectid}/vpcs/{vpcid}/subnets/{subnetid}"})
    public SubnetState updateSubnetState(@PathVariable String projectid, @PathVariable String vpcid, @PathVariable String subnetid, @RequestBody SubnetState resource) throws Exception {

        SubnetState subnetState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditions.verifyResourceNotNull(resource);
            RestPreconditions.populateResourceProjectId(resource, projectid);
            RestPreconditions.populateResourceVpcId(resource, vpcid);

            subnetState = this.subnetRedisRepository.findItem(subnetid);
            if(subnetState == null){
                throw new ResourceNotFoundException("Subnet not found : " + subnetid);
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            this.subnetRedisRepository.addItem(resource);
            subnetState = this.subnetRedisRepository.findItem(subnetid);

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ResourceNotFoundException e){
            throw new Exception(e);
        }catch (ParameterUnexpectedValueException e){
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
            if(subnetState == null){
                return;
            }

            RestPreconditions.verifyParameterEqual(subnetState.getProjectId(), projectid);
            RestPreconditions.verifyParameterEqual(subnetState.getVpcId(), vpcid);

            subnetRedisRepository.deleteItem(subnetid);

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ParameterUnexpectedValueException e){
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
                    .collect(Collectors.toMap(state -> state.getKey(), state-> state.getValue()));

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ResourceNotFoundException e){
            throw new Exception(e);
        }

        return subnetStates;
    }


}
