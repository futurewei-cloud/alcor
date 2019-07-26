package com.futurewei.alioth.controller.web;

import java.util.concurrent.atomic.AtomicLong;

import com.futurewei.alioth.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alioth.controller.comm.message.MessageClient;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate;
import com.futurewei.alioth.controller.schema.Vpc;
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
    private final VpcState defaultVpc = new VpcState("AwesomeProject", "ComplicatedId", "AwesomeVpc", "172.0.0.1/24");

//    @Autowired
//    private VpcRedisRepository vpcRedisRepository;

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpc/{vpcid}")
    public VpcState getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) {
//        VpcState vpcState = vpcRedisRepository.findItem(vpcid);
//        if(vpcState == null){
//            return defaultVpc;
//        }
//        return vpcState;
        final Vpc.VpcState vpc_state = GoalStateUtil.CreateVpcState(Common.OperationType.CREATE,
                projectid,
                vpcid,
                "SuperVpc",
                "192.168.0.0/24");

        Goalstate.GoalState goalstate = Goalstate.GoalState.newBuilder()
                .addVpcStates(vpc_state)
                .build();

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topic = "hostid-fb06a9ea-db99-4a48-8143-58c74fa6ef43";
        client.runProducer(topic, goalstate);

        return new VpcState(projectid, vpcid, "SuperVpc", "192.168.0.0/24");
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
    public VpcState createVpcState(@RequestBody VpcState resource) {
        RestPreconditions.checkNotNull(resource);
//        vpcRedisRepository.addItem(resource);
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