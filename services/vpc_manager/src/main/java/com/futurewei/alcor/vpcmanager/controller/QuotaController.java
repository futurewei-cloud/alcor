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
package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import com.futurewei.alcor.web.entity.quota.QuotaWebJson;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

////////////////////////////////////////////////////////////////////
//NOTE: QuotaController is hosted temporarily in VPC Mgr
//      before Quote Mgr is deployed
////////////////////////////////////////////////////////////////////
@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class QuotaController {

    /**
     * Lists quota to which the project has access
     *
     * @param projectId
     * @return QuotaWebJson
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = "/project/{projectId}/quotas/{projectId}")
    @DurationStatistics
    public QuotaWebJson getQuotaByProjectId(@PathVariable String projectId) throws Exception {

        QuotaEntity defaultQuota = this.createDefaultQuotaEntity();

        return new QuotaWebJson(defaultQuota);
    }

    private QuotaEntity createDefaultQuotaEntity() {
        return new QuotaEntity(-1, -1, -1, -1, 10,
                -1, -1, -1, -1);
    }
}
