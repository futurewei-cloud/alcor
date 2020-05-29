/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.common.constants.Common;
import com.futurewei.alcor.web.entity.gsinfo.GoalStateForNorth;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class GSController {
    /**
     * Accept north bound calls then transfer to ACA calls in southbound
     *
     * @param gs Encapsulation of GSInfo message
     * @return RestOperationResult in String
     * @throws Exception Various exceptions that may occur during the create
     *                   process
     * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
     * /pages/infra_services/data_plane_manager.adoc
     */
    @RequestMapping(method = POST, value = {"/goalstate/{gsinfo}", "v4" +
            "/goalstate/{gsinfo}"})
    @ResponseStatus(HttpStatus.CREATED)
    public synchronized String[] create(@RequestBody GoalStateForNorth gs) throws Exception {
        gs.setOpType(Common.OperationType.CREATE);
        return service(gs);
    }

    /**
     * Accept north bound calls then transfer to ACA calls in southbound
     *
     * @param gs Encapsulation of GSInfo message
     * @return RestOperationResult in String
     * @throws Exception Various exceptions that may occur during the update
     *                   process
     * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
     * /pages/infra_services/data_plane_manager.adoc
     */
    @RequestMapping(method = PUT, value = {"/goalstate/{gsinfo}", "v4" +
            "/goalstate/{gsinfo}"})
    public synchronized String[] update(@RequestBody GoalStateForNorth gs) throws Exception {

        gs.setOpType(Common.OperationType.UPDATE);
        return service(gs);
    }

    /**
     * Accept north bound calls then transfer to ACA calls in southbound
     *
     * @param gs Encapsulation of GSInfo message
     * @return RestOperationResult in String
     * @throws Exception Various exceptions that may occur during the delete
     *                   process
     * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
     * /pages/infra_services/data_plane_manager.adoc
     */
    @RequestMapping(method = DELETE, value = {"/project/goalstate/{gsinfo}",
            "v4/goalstate/{gsinfo}"})
    public synchronized String[] delete(@RequestBody GoalStateForNorth gs) throws Exception {
        gs.setOpType(Common.OperationType.DELETE);
        return service(gs);
    }

    private synchronized String[] service(GoalStateForNorth gs) throws Exception {
        // TODO: Create a verification framework for all resources
        return GoalStateUtil.talkToACA(
                GoalStateUtil.transformNorthToSouth(gs),
                gs.isFastPath()).toArray(new String[0]);


    }

}
