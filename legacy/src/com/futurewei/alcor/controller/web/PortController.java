/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.app.onebox.*;
import com.futurewei.alcor.controller.db.repo.PortRedisRepository;
import com.futurewei.alcor.controller.db.repo.SubnetRedisRepository;
import com.futurewei.alcor.controller.db.repo.VpcRedisRepository;
import com.futurewei.alcor.controller.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.controller.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.controller.exception.ResourceNotFoundException;
import com.futurewei.alcor.controller.exception.ResourceNullException;
import com.futurewei.alcor.controller.model.*;
import com.futurewei.alcor.controller.web.util.ControllerUtil;
import com.futurewei.alcor.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            value = {"/project/{projectid}/ports/{portId}", "v4/{projectid}/ports/{portId}"})
    public PortStateJson getPortStateById(@PathVariable String projectid, @PathVariable String portId) throws Exception {

        PortState portState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portId);
            RestPreconditions.verifyResourceFound(projectid);

            portState = this.portRedisRepository.findItem(portId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (portState == null) {
            //TODO: REST error code
            return new PortStateJson();
        }

        return new PortStateJson(portState);
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/ports", "v4/{projectid}/ports"})
    @ResponseStatus(HttpStatus.CREATED)
    public PortStateJson createPortState(@PathVariable String projectid, @RequestBody PortStateJson resource) throws Exception {

        long T0 = System.nanoTime();
        PortState customerPortState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceFound(projectid);

            // TODO: Create a verification framework for all resources
            PortState portState = resource.getPort();
            RestPreconditions.verifyResourceNotNull(portState);
            RestPreconditions.verifyResourceFound(portState.getNetworkId());
            RestPreconditions.verifyResourceNotExists(portState.getId());
            RestPreconditions.populateResourceProjectId(portState, projectid);

            long T1 = System.nanoTime();

            if (OneBoxConfig.IS_K8S) {
                customerPortState = ControllerUtil.CreatePort(portState);
            } else if (OneBoxConfig.IS_Onebox) {
                long[] times = OneBoxUtil.CreatePort(portState);
                RestPreconditions.recordRequestTimeStamp(portState.getId(), T0, T1, times);
                customerPortState = portState;
            }

            this.portRedisRepository.addItem(customerPortState);

        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new PortStateJson(customerPortState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/ports/{portid}", "v4/{projectid}/ports/{portid}"})
    public PortStateJson updateSubnetState(@PathVariable String projectid, @PathVariable String portid, @RequestBody PortStateJson resource) throws Exception {

        PortState currentPortState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);

            PortState updatedPortState = resource.getPort();
            RestPreconditions.verifyResourceNotNull(updatedPortState);
            RestPreconditions.verifyResourceFound(updatedPortState.getNetworkId());
            RestPreconditions.populateResourceProjectId(updatedPortState, projectid);

            currentPortState = this.portRedisRepository.findItem(portid);
            if (currentPortState == null) {
                throw new ResourceNotFoundException("Port not found : " + portid);
            }

            RestPreconditions.verifyParameterEqual(currentPortState.getProjectId(), projectid);

            this.portRedisRepository.addItem(updatedPortState);
            currentPortState = this.portRedisRepository.findItem(portid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new PortStateJson(currentPortState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/ports/{portid}", "v4/{projectid}/ports/{portid}"})
    public ResponseId deletePortState(@PathVariable String projectid, @PathVariable String portid) throws Exception {

        PortState portState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(portid);

            portState = this.portRedisRepository.findItem(portid);
            if (portState == null) {
                return new ResponseId();
            }

            RestPreconditions.verifyParameterEqual(portState.getProjectId(), projectid);

            portRedisRepository.deleteItem(portid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ParameterUnexpectedValueException e) {
            throw new Exception(e);
        }

        return new ResponseId(portid);
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
                    .collect(Collectors.toMap(state -> state.getKey(), state -> state.getValue()));

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return portStates;
    }

    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/portgroup"},
            consumes = "application/json",
            produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PortState> createPortStates(@PathVariable String projectid, @RequestBody PortStateGroup resourceGroup) throws Exception {

        long T0 = System.nanoTime();
        List<PortState> response = new ArrayList<>();

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyResourceFound(projectid);

            List<PortState> portStates = resourceGroup.getPortStates();
            for (PortState state : portStates) {
                this.portRedisRepository.addItem(state);
                response.add(state);
            }
            long T1 = System.nanoTime();

            if (OneBoxConfig.IS_Onebox) {
                long[][] elapsedTimes = OneBoxUtil.CreatePortGroup(resourceGroup);
                int hostCount = elapsedTimes.length;

                long averageElapseTime = 0, minElapseTime = Long.MAX_VALUE, maxElapseTime = Long.MIN_VALUE;
                System.out.println("Total number of time sequences:" + hostCount);
                for (int i = 0; i < hostCount; i++) {
                    long et = elapsedTimes[i][2] - T0;
                    averageElapseTime += et;
                    if (et < minElapseTime) minElapseTime = et;
                    if (et > maxElapseTime) maxElapseTime = et;
                    RestPreconditions.recordRequestTimeStamp(resourceGroup.getPortState(i).getId(), T0, T1, elapsedTimes[i]);
                }

                OneBoxConfig.TIME_STAMP_WRITER.newLine();
                OneBoxConfig.TIME_STAMP_WRITER.write("," + averageElapseTime / (1000000 * hostCount) + "," + minElapseTime / 1000000 + "," + maxElapseTime / 1000000);
                OneBoxConfig.TIME_STAMP_WRITER.newLine();
                OneBoxConfig.TIME_STAMP_WRITER.write("Average time of " + OneBoxConfig.TOTAL_REQUEST + " requests :" + OneBoxConfig.TOTAL_TIME / OneBoxConfig.TOTAL_REQUEST + " ms");
                if (OneBoxConfig.TIME_STAMP_WRITER != null)
                    OneBoxConfig.TIME_STAMP_WRITER.close();

            }
        } catch (Exception e) {
            throw e;
        }

        return response;
    }

}
