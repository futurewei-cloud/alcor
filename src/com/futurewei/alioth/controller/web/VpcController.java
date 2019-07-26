package com.futurewei.alioth.controller.web;

import java.util.concurrent.atomic.AtomicLong;

import com.futurewei.alioth.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alioth.controller.comm.message.MessageClient;
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
    private final VpcState defaultVpc = new VpcState("DefaultProject", "DefaultId", "DefaultVpc", "100.0.0.1/24");

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpc/{vpcid}")
    public VpcState getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) {
        VpcState vpcState = vpcRedisRepository.findItem(vpcid);
        if(vpcState == null){
            return defaultVpc;
        }

        return vpcState;
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpcs}")
    public VpcState getAllVpcStates(@PathVariable String projectid) {
        return new VpcState(projectid, "", "AwesomeVpc", "172.0.0.1/24");
    }

    @RequestMapping(
            method = POST,
            value = "/project/{projectid}/vpc")
    @ResponseStatus(HttpStatus.CREATED)
    public VpcState createVpcState(@RequestBody VpcState resource) throws Exception {
        RestPreconditions.checkNotNull(resource);
        vpcRedisRepository.addItem(resource);

        //TODO: Algorithm to determine the hosts
        HostInfo host1 = new HostInfo("hostid_1", "host1", new byte[]{10,0,0,1});
        HostInfo host2 = new HostInfo("hostid_1", "host1", new byte[]{10,0,0,2});

        Goalstate.GoalState goalstate = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE,
                resource,
                host1.getIpAddress(),
                host2.getIpAddress());

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topic = MessageClient.getGoalStateTopic(host1.getId());
        client.runProducer(topic, goalstate);

        return new VpcState(resource);
    }

    @RequestMapping(
            method = GET,
            value = "/debug")
    public DebugInfo getDebugInfo(@RequestParam(value="name", defaultValue="World") String name) {
        return new DebugInfo(counter.incrementAndGet(),
                String.format(template, name));
    }
}