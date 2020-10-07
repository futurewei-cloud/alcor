/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.subnet.rbac;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.rbac.OwnerChecker;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.rbac.aspect.OwnerCheckerSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SubnetOwnerCheckerSupplier implements OwnerCheckerSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(SubnetOwnerCheckerSupplier.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SubnetDatabaseService subnetService;

    @Override
    public OwnerChecker getOwnerChecker() {
        return () -> {
            String path = request.getServletPath();
            // only check foreign api
            if (!path.startsWith("/project")) {
                return true;
            }
            String[] pathInfo = path.split("/");
            if (pathInfo.length < 5) {
                return true;
            }
            String projectId = pathInfo[2];
            String subnetId = pathInfo[4];
            try {
                SubnetEntity subnetEntity = subnetService.getBySubnetId(subnetId);
                return projectId.equals(subnetEntity.getProjectId());
            } catch (ResourceNotFoundException | ResourcePersistenceException e) {
                LOG.error("get subnet from db error {}", e.getMessage());
                return false;
            }
        };
    }
}
