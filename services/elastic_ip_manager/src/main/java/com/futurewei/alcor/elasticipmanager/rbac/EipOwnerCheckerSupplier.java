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

package com.futurewei.alcor.elasticipmanager.rbac;

import com.futurewei.alcor.common.rbac.OwnerChecker;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.rbac.aspect.OwnerCheckerSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class EipOwnerCheckerSupplier implements OwnerCheckerSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(EipOwnerCheckerSupplier.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ElasticIpService eipService;

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
            String eipId = pathInfo[4];
            try {
                ElasticIpInfo eipInfo = eipService.getElasticIp(projectId, eipId);
                return projectId.equals(eipInfo.getProjectId());
            } catch (Exception e) {
                LOG.error("get eip from db error {}", e.getMessage());
                return false;
            }
        };
    }
}
