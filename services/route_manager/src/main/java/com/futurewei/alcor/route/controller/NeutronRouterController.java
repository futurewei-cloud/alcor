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
import com.futurewei.alcor.route.entity.RouteConstant;
import com.futurewei.alcor.route.exception.CanNotFindRouter;
import com.futurewei.alcor.route.service.NeutronRouterService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.route.utils.NeutronRouteUtil;
import com.futurewei.alcor.route.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NeutronRouterController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    @Autowired
    private NeutronRouterService neutronRouterService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Show a Neutron router
     * @param routerId
     * @param projectid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routers/{routerId}"})
    @DurationStatistics
    public NeutronRouterWebJson getNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerId) throws Exception {

        NeutronRouterWebRequestObject neutronRouterWebRequestObject = null;

//        Router router = null;
//        RouterExtraAttribute routerExtraAttribute = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(routerId);
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            neutronRouterWebRequestObject = this.neutronRouterService.getNeutronRouter(routerId);

//            router = this.routerDatabaseService.getByRouterId(routerId);
//            if (router == null) {
//                return new NeutronRouterWebJson();
//            }
//
//            routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());
//
//            BeanUtils.copyProperties(router, neutronRouterWebRequestObject);
//            if (routerExtraAttribute != null) {
//                BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
//            }

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindRouter e) {
            logger.error(e.getMessage() + " : " + routerId);
            return new NeutronRouterWebJson();
        }

        return new NeutronRouterWebJson(neutronRouterWebRequestObject);
    }

    /**
     * List Neutron routers
     * @param projectid
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/project/{projectid}/routers"})
    @DurationStatistics
    public NeutronRoutersWebJson getNeutronRouters(@PathVariable String projectid) throws Exception {

        List<NeutronRouterWebRequestObject> neutronRouters = new ArrayList<>();

        Map<String, Router> routers = null;
        RouterExtraAttribute routerExtraAttribute = null;

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), Router.class);

        ControllerUtil.handleUserRoles(request.getHeader(ControllerUtil.TOKEN_INFO_HEADER), queryParams);
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);
            RestPreconditionsUtil.verifyResourceFound(projectid);

            routers = this.routerDatabaseService.getAllRouters(queryParams);
            if (routers == null) {
                return new NeutronRoutersWebJson();
            }

            for (Map.Entry<String, Router> entry : routers.entrySet()) {
                NeutronRouterWebRequestObject neutronRouterWebRequestObject = new NeutronRouterWebRequestObject();
                Router router = (Router) entry.getValue();
                routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());

                BeanUtils.copyProperties(router, neutronRouterWebRequestObject);
                if (routerExtraAttribute != null) {
                    BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
                }

                neutronRouters.add(neutronRouterWebRequestObject);
            }

        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (ResourceNotFoundException e) {
            throw new Exception(e);
        }

        return new NeutronRoutersWebJson(neutronRouters);
    }

    /**
     * Create a Neutron router
     * @param projectid
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = POST,
            value = {"/project/{projectid}/routers"})
    @DurationStatistics
    public NeutronRouterWebJson createNeutronRouters(@PathVariable String projectid, @RequestBody NeutronRouterWebJson resource) throws Exception {

        NeutronRouterWebRequestObject neutronRouterWebRequestObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            // TODO: check resource

            neutronRouterWebRequestObject = resource.getNeutronRouterWebRequestObject();
            RestPreconditionsUtil.verifyResourceNotNull(neutronRouterWebRequestObject);
            String id = neutronRouterWebRequestObject.getId();

            if (id == null || StringUtils.isEmpty(id)) {
                UUID vpcId = UUID.randomUUID();
                neutronRouterWebRequestObject.setId(vpcId.toString());
            }

            // TODO: configure default value

        } catch (Exception e) {
            throw e;
        }

        return new NeutronRouterWebJson(neutronRouterWebRequestObject);
    }

    /**
     * Update a Neutron router
     * @param projectid
     * @param routerId
     * @param resource
     * @return
     * @throws Exception
     */
    @RequestMapping(
            method = PUT,
            value = {"/project/{projectid}/routers/{routerId}"})
    @DurationStatistics
    public NeutronRouterWebJson updateNeutronRouterByRouterId(@PathVariable String projectid,@PathVariable String routerId, @RequestBody NeutronRouterWebJson resource) throws Exception {
        NeutronRouterWebRequestObject neutronRouterWebRequestObject = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectid);

            // TODO: check resource

            neutronRouterWebRequestObject = resource.getNeutronRouterWebRequestObject();
            NeutronRouterWebRequestObject inNeutronRouter = this.neutronRouterService.getNeutronRouter(routerId);

            NeutronRouteUtil.copyPropertiesIgnoreNull(neutronRouterWebRequestObject, inNeutronRouter);

            // TODO: configure default value

        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CanNotFindRouter e) {
            throw e;
        }

        return new NeutronRouterWebJson(neutronRouterWebRequestObject);
    }

}
