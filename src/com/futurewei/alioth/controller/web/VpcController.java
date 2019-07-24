package com.futurewei.alioth.controller.web;

import java.util.concurrent.atomic.AtomicLong;

import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class VpcController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/vpc/{vpcid}")
    public VpcState getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) {
        return new VpcState(projectid, vpcid, "AwesomeVpc", "172.0.0.1/24");
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
        return new VpcState(resource.getProjectId(), resource.getId(), resource.getName(), resource.getCidr());
    }

    @RequestMapping(
            method = GET,
            value = "/debug")
    public DebugInfo getDebugInfo(@RequestParam(value="name", defaultValue="World") String name) {
        return new DebugInfo(counter.incrementAndGet(),
                String.format(template, name));
    }
}