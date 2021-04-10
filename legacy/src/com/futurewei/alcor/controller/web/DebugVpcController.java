/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.controller.web;

import com.futurewei.alcor.controller.db.repo.VpcRepository;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.db.Transaction;
import com.futurewei.alcor.controller.exception.*;
import com.futurewei.alcor.controller.model.ResponseId;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.model.VpcStateJson;
import com.futurewei.alcor.controller.web.util.RestPreconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
public class DebugVpcController {
    @Autowired(required = false)
    private VpcRepository vpcRepository;

    @RequestMapping(
            method = GET,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson getVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcRepository.findItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (vpcState == null) {
            //TODO: REST error code
            return new VpcStateJson();
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpcs")
    public Map getVpcCountAndAllVpcStates() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcRepository.findAllItems();
        result.put("Count", dataItems.size());
        result.put("Vpcs", dataItems);

        return result;
    }

    @RequestMapping(
            method = GET,
            value = "/debug/project/all/vpccount")
    public Map getVpcCount() throws CacheException {
        Map result = new HashMap<String, Object>();
        Map dataItems = vpcRepository.findAllItems();
        result.put("Count", dataItems.size());

        return result;
    }

    @RequestMapping(
            method = POST,
            value = {"/debug/project/{projectid}/vpcs"})
    @ResponseStatus(HttpStatus.CREATED)
    public VpcStateJson createVpcState(@PathVariable String projectid, @RequestBody VpcStateJson resource) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditions.verifyResourceNotNull(inVpcState);
            RestPreconditions.populateResourceProjectId(inVpcState, projectid);

            Transaction transaction = this.vpcRepository.getCache().getTransaction();
            transaction.start();

            this.vpcRepository.addItem(inVpcState);
            vpcState = this.vpcRepository.findItem(inVpcState.getId());

            transaction.commit();

            if (vpcState == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNullException e) {
            throw new Exception(e);
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = PUT,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public VpcStateJson updateVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid, @RequestBody VpcStateJson resource) throws Exception {

        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);

            VpcState inVpcState = resource.getVpc();
            RestPreconditions.verifyResourceNotNull(inVpcState);
            RestPreconditions.populateResourceProjectId(inVpcState, projectid);
            RestPreconditions.populateResourceVpcId(inVpcState, vpcid);

            vpcState = this.vpcRepository.findItem(vpcid);
            if (vpcState == null) {
                throw new ResourceNotFoundException("Vpc not found : " + vpcid);
            }

            this.vpcRepository.addItem(inVpcState);

            vpcState = this.vpcRepository.findItem(vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new VpcStateJson(vpcState);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/debug/project/{projectid}/vpcs/{vpcid}"})
    public ResponseId deleteVpcStateByVpcId(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {
        VpcState vpcState = null;

        try {
            RestPreconditions.verifyParameterNotNullorEmpty(projectid);
            RestPreconditions.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditions.verifyResourceFound(projectid);

            vpcState = this.vpcRepository.findItem(vpcid);
            if (vpcState == null) {
                return new ResponseId();
            }

            vpcRepository.deleteItem(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        return new ResponseId(vpcid);
    }
}
