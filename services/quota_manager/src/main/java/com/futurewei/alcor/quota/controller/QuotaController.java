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

package com.futurewei.alcor.quota.controller;

import com.futurewei.alcor.quota.service.QuotaService;
import com.futurewei.alcor.web.entity.quota.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class QuotaController {

    private static final Logger LOG = LoggerFactory.getLogger(QuotaController.class);

    @Autowired
    private QuotaService quotaService;

    /**
     * get all non-default quotas
     * @return all non-default quotas
     */
    @GetMapping({"/project/{project_id}/quotas", "/v4/{project_id}/quotas"})
    public QuotaWebsJson getAllQuotas() throws Exception {
        List<QuotaEntity> quotaEntities = quotaService.findAllQuotas();
        return new QuotaWebsJson(quotaEntities);
    }

    /**
     * lists quotas for a object
     * @param projectId the project id
     * @return the project quota
     */
    @GetMapping({"/project/{req_project_id}/quotas/{project_id}", "/v4/{req_project_id}/quotas/{project_id}"})
    public QuotaWebJson getQuota(@PathVariable("project_id") String projectId) throws Exception {
        QuotaEntity quotaEntity = quotaService.findQuotaByProjectId(projectId);
        return new QuotaWebJson(quotaEntity);
    }

    /**
     * updates quotas for a project, Use when non-default quotas are desired.
     * @param projectId the project id
     * @param quotaWebJsonRequest quota detail for project create
     * @return the new quota
     */
    @PutMapping({"/project/{req_project_id}/quotas/{project_id}", "/v4/{req_project_id}/quotas/{project_id}"})
    public QuotaWebJson updateQuota(@PathVariable("project_id") String projectId,
                                    @RequestBody QuotaWebJson  quotaWebJsonRequest) throws Exception {
        LOG.info("update quotas for project {}", projectId);
        QuotaEntity quotaEntity = quotaService.updateQuota(quotaWebJsonRequest.getQuota());
        LOG.info("update quotas for project {} success", projectId);
        return new QuotaWebJson(quotaEntity);
    }

    /**
     * resets quotas to default values for a project.
     * @param projectId the project id
     */
    @DeleteMapping({"/project/{req_project_id}/quotas/{project_id}", "/v4/{req_project_id}/quotas/{project_id}"})
    public void deleteQuota(@PathVariable("project_id") String projectId) throws Exception {
        quotaService.deleteQuotaByProjectId(projectId);
    }

    /**
     * lists default quotas for a project.
     * @return the default quota
     */
    @GetMapping({"/project/{req_project_id}/quotas/{project_id}/default",
            "/v4/{req_project_id}/quotas/{project_id}/default"})
    public QuotaWebJson getDefaultQuotaForProject() throws Exception {
        QuotaEntity quotaEntity = quotaService.getDefault();
        return new QuotaWebJson(quotaEntity);
    }

    /**
     * shows quota details for a project.
     * @param projectId the project id
     * @return resource quota details
     */
    @GetMapping({"/project/{req_project_id}/quotas/{project_id}/details.json",
            "/v4/{req_project_id}/quotas/{project_id}/details.json"})
    public QuotaDetailWebJson showProjectQuotaDetail(@PathVariable("project_id") String projectId) throws Exception {
        QuotaDetailEntity quotaDetailEntity = quotaService.findQuotaDetailByProjectId(projectId);
        return new QuotaDetailWebJson(quotaDetailEntity);
    }

}
