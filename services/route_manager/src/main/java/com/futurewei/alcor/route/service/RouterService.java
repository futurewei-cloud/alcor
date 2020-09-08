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
import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.exception.VpcRouterContainsSubnetRoutingTables;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface RouterService {

    public Router getOrCreateVpcRouter (String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException;
    public Router createDefaultVpcRouter (String projectId, VpcEntity vpcEntity) throws DatabasePersistenceException;
    public String deleteVpcRouter (String projectId, String vpcId) throws Exception;

}
