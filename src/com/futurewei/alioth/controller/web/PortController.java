package com.futurewei.alioth.controller.web;

import com.futurewei.alioth.controller.app.demo.DemoUtil;
import com.futurewei.alioth.controller.cache.repo.PortRedisRepository;
import com.futurewei.alioth.controller.cache.repo.SubnetRedisRepository;
import com.futurewei.alioth.controller.cache.repo.VpcRedisRepository;
import com.futurewei.alioth.controller.exception.ParameterNullOrEmptyException;
import com.futurewei.alioth.controller.exception.ParameterUnexpectedValueException;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;
import com.futurewei.alioth.controller.exception.ResourceNullException;
import com.futurewei.alioth.controller.model.PortState;
import com.futurewei.alioth.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static com.futurewei.alioth.controller.app.demo.DemoConfig.IS_Demo;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
public class PortController {

    @Autowired
    private VpcRedisRepository vpcRedisRepository;

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    @Autowired
    private PortRedisRepository portRedisRepository;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/port/{portId}", "v4/{projectid}/ports/{portId}"})
    public PortState getPortStateById(@PathVariable String projectid, @PathVariable String portId) throws Exception {

        PortState portState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portId);
            RestPreconditions.verifyResourceFound(projectid);

            portState = this.portRedisRepository.findItem(portId);
        }catch (ParameterNullOrEmptyException e){
            //TODO: REST error code
            throw new Exception(e);
        }

        if(portState == null){
            //TODO: REST error code
            return new PortState();
        }

        return portState;
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/port", "v4/{projectid}/ports"})
    @ResponseStatus(HttpStatus.CREATED)
    public PortState createPortState(@PathVariable String projectid, @RequestBody PortState resource) throws Exception {

        long T0 = System.nanoTime();

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceNotNull(resource);
            RestPreconditions.verifyResourceFound(projectid);

            // TODO: Create a verification framework for all resources
            RestPreconditions.verifyResourceFound(resource.getNetworkId());
            RestPreconditions.populateResourceProjectId(resource, projectid);

            this.portRedisRepository.addItem(resource);
            long T1 = System.nanoTime();

            if(IS_Demo) {
                long[] times = DemoUtil.CreatePort(resource);
                RestPreconditions.recordRequestTimeStamp(resource.getId(), T0, T1, times);
            }
        }
        catch (ResourceNullException e){
            throw new Exception(e);
        }

        return new PortState(resource);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/port/{portid}", "v4/{projectid}/ports/{portid}"})
    public PortState updateSubnetState(@PathVariable String projectid, @PathVariable String portid, @RequestBody PortState resource) throws Exception {

        PortState portState = null;

        try{
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);
            RestPreconditions.verifyResourceNotNull(resource);

            RestPreconditions.verifyResourceFound(resource.getNetworkId());
            RestPreconditions.populateResourceProjectId(resource, projectid);

            portState = this.portRedisRepository.findItem(portid);
            if(portState == null){
                throw new ResourceNotFoundException("Port not found : " + portid);
            }

            RestPreconditions.verifyParameterEqual(portState.getProjectId(), projectid);

            this.portRedisRepository.addItem(resource);
            portState = this.portRedisRepository.findItem(portid);

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ResourceNotFoundException e){
            throw new Exception(e);
        }catch (ParameterUnexpectedValueException e){
            throw new Exception(e);
        }

        return portState;
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/port/{portid}", "v4/{projectid}/ports/{portid}"})
    public void deletePortState(@PathVariable String projectid, @PathVariable String portid) throws Exception {

        PortState portState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);

            portState = this.portRedisRepository.findItem(portid);
            if(portState == null){
                return;
            }

            RestPreconditions.verifyParameterEqual(portState.getProjectId(), projectid);

            portRedisRepository.deleteItem(portid);

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ParameterUnexpectedValueException e){
            throw new Exception(e);
        }
    }

    @RequestMapping(
            method = GET,
            value = "/project/{projectid}/subnet/{subnetid}/ports")
    public Map gePortStatesByProjectIdAndSubnetId(@PathVariable String projectid, @PathVariable String subnetid) throws Exception {
        Map<String, PortState> portStates = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(subnetid);
            RestPreconditions.verifyResourceFound(projectid);
            RestPreconditions.verifyResourceFound(subnetid);

            portStates = this.portRedisRepository.findAllItems();
            portStates = portStates.entrySet().stream()
                    .filter(state -> projectid.equalsIgnoreCase(state.getValue().getProjectId())
                            && subnetid.equalsIgnoreCase(state.getValue().getNetworkId()))
                    .collect(Collectors.toMap(state -> state.getKey(), state-> state.getValue()));

        }catch (ParameterNullOrEmptyException e){
            throw new Exception(e);
        }catch (ResourceNotFoundException e){
            throw new Exception(e);
        }

        return portStates;
    }


}
