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
package com.futurewei.alcor.route.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.logging.*;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.exception.CanNotFindRouter;
import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.exception.VpcRouterContainsSubnetRoutingTables;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterService;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebJson;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebRequestObject;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.route.RouterWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class RouterController {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterService routerService;

    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public RouterWebJson getOrCreateVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        Router router = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            router = this.routerService.getOrCreateVpcRouter(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (DatabasePersistenceException e) {
            throw e;
        }

        return new RouterWebJson(router);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/project/{projectid}/vpcs/{vpcid}/router"})
    @DurationStatistics
    public ResponseId deleteVpcRouter(@PathVariable String projectid, @PathVariable String vpcid) throws Exception {

        String routerId = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routerId = this.routerService.deleteVpcRouter(projectid, vpcid);

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindVpc e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        } catch (VpcRouterContainsSubnetRoutingTables e) {
            logger.log(Level.WARNING, e.getMessage() + " : " + vpcid);
            throw e;
        }

        return new ResponseId(routerId);
    }

}
