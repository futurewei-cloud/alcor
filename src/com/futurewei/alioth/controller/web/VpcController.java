package com.futurewei.alioth.controller.web;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.futurewei.alioth.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alioth.controller.comm.message.MessageClient;
import com.futurewei.alioth.controller.exception.*;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class VpcController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpc/{vpcid}")
    public VpcState getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try{
            RestPreconditions.checkNotNullorEmpty(projectid);
            RestPreconditions.checkNotNullorEmpty(vpcid);

            vpcState = this.vpcRedisRepository.findItem(vpcid);
        }catch (ResourceNullOrEmptyException e){
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
            value = "/project/{projectid}/vpc")
    @ResponseStatus(HttpStatus.CREATED)
    public VpcState createVpcState(@PathVariable String projectid, @RequestBody VpcState resource) throws Exception {
        try{
            RestPreconditions.checkNotNullorEmpty(projectid);
            RestPreconditions.checkNotNull(resource);

            this.vpcRedisRepository.addItem(resource);

            //TODO: Algorithm to determine the hosts
            HostInfo trHost1 = new HostInfo("tr_hostid_1", "host1", new byte[]{10,0,0,1});
            HostInfo trHost2 = new HostInfo("tr_hostid_2", "host1", new byte[]{10,0,0,2});

            Goalstate.GoalState goalstate = GoalStateUtil.CreateGoalState(
                    Common.OperationType.CREATE,
                    resource,
                    trHost1.getIpAddress(),
                    trHost2.getIpAddress());

            MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
            String topic = MessageClient.getGoalStateTopic(trHost1.getId());
            client.runProducer(topic, goalstate);
        }
        catch (ResourceNullOrEmptyException e){
            throw new Exception(e);
        }
        catch (ResourceNullException e){
            throw new Exception(e);
        }

        return new VpcState(resource);
    }

//    @RequestMapping(
//            method = PUT,
//            value = "/project/{projectid}/vpc/{vpcid}")
//    public VpcState updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcState resource) throws Exception {
//
//        VpcState vpcState = null;
//
//        try{
//            RestPreconditions.checkNotNullorEmpty(projectid);
//            RestPreconditions.checkNotNullorEmpty(vpcid);
//
//            vpcState = this.vpcRedisRepository.findItem(vpcid);
//            if(vpcState == null){
//                return new VpcState();
//            }
//
//            this.vpcRedisRepository.addItem(resource);
//
//            vpcState = this.vpcRedisRepository.findItem(vpcid);
//        }catch (ResourceNullOrEmptyException e){
//            throw new Exception(e);
//        }
//
//        return vpcState;
//    }
//
//    @RequestMapping(
//            method = DELETE,
//            value = "/project/{projectid}/vpc/{vpcid}")
//    public void deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
//        VpcState vpcState = null;
//
//        try {
//            RestPreconditions.checkNotNullorEmpty(projectid);
//            RestPreconditions.checkNotNullorEmpty(vpcid);
//
//            vpcState = this.vpcRedisRepository.findItem(vpcid);
//            if (vpcState == null) {
//                // TODO: REST error code
//                return;
//            }
//
//            vpcRedisRepository.deleteItem(vpcid);
//        }catch (ResourceNullOrEmptyException e){
//            throw new Exception(e);
//        }
//    }

    @RequestMapping(
            method = GET,
            value = "/project/all/vpcs")
    public Map getAllVpcStates() {
        return this.vpcRedisRepository.findAllItems();
    }

    @RequestMapping(
            method = GET,
            value = "/debug")
    public DebugInfo getDebugInfo(@RequestParam(value="name", defaultValue="World") String name) {
        return new DebugInfo(counter.incrementAndGet(),
                String.format(template, name));
    }
}