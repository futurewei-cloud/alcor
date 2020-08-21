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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.CanNotFindRouter;
import com.futurewei.alcor.route.service.NeutronRouterService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebJson;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebRequestObject;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.route.RouterExtraAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NeutronRouterServiceImpl implements NeutronRouterService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouterDatabaseService routerDatabaseService;

    @Autowired
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    @Override
    public NeutronRouterWebRequestObject getNeutronRouter(String routerId) throws ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter {
        NeutronRouterWebRequestObject neutronRouterWebRequestObject = new NeutronRouterWebRequestObject();

        Router router = this.routerDatabaseService.getByRouterId(routerId);
        if (router == null) {
            throw new CanNotFindRouter();
        }

        RouterExtraAttribute routerExtraAttribute = this.routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(router.getRouterExtraAttributeId());

        BeanUtils.copyProperties(router, neutronRouterWebRequestObject);
        if (routerExtraAttribute != null) {
            BeanUtils.copyProperties(routerExtraAttribute, neutronRouterWebRequestObject);
        }
        return neutronRouterWebRequestObject;
    }

}
