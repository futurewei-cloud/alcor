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

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.route.RouterWebJson;
import com.futurewei.alcor.web.entity.route.RoutersWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NeutronRouterController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Show a Neutron router
     * @param routerId
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routers/{routerId}"})
    @DurationStatistics
    public RouterWebJson getNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerId) throws Exception {

        Router router = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            router = this.routerDatabaseService.getByRouterId(routerId);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (router == null) {
            //TODO: REST error code
            return new RouterWebJson();
        }

        return new RouterWebJson(router);
    }

    /**
     * List Neutron routers
     * @param projectId
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectId}/routers"})
    @DurationStatistics
    public RoutersWebJson getNeutronRouters(@PathVariable String projectId) throws Exception {

        Map<String, Router> routers = null;

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), Router.class);

        ControllerUtil.handleUserRoles(request.getHeader(ControllerUtil.TOKEN_INFO_HEADER), queryParams);
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
            RestPreconditionsUtil.verifyResourceFound(projectId);

            routers = this.routerDatabaseService.getAllRouters(queryParams);

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return new RoutersWebJson(new ArrayList<>(routers.values()));
    }

    /**
     * Create a Neutron router
     * @param projectId
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectId}/routers"})
    @DurationStatistics
    public RouterWebJson createNeutronRouters(@PathVariable String projectId) throws Exception {
        return new RouterWebJson();
    }

    /**
     * Update a Neutron router
     * @param projectid
     * @param routerId
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerId}"})
    @DurationStatistics
    public RouterWebJson updateNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerId) throws Exception {
        return new RouterWebJson();
    }

}
