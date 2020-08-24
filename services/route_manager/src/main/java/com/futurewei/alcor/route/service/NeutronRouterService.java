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
package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.CanNotFindRouter;
import com.futurewei.alcor.route.exception.NeutronRouterIsNull;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebRequestObject;

public interface NeutronRouterService {

    public NeutronRouterWebRequestObject getNeutronRouter (String routerId) throws ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter;
    public void saveRouterAndRouterExtraAttribute (NeutronRouterWebRequestObject neutronRouter) throws NeutronRouterIsNull, DatabasePersistenceException;
}
