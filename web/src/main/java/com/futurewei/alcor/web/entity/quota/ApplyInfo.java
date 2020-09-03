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

package com.futurewei.alcor.web.entity.quota;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApplyInfo {

    @JsonProperty("apply_id")
    private String applyId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("resource_deltas")
    private List<ResourceDelta> resourceDeltas;

    public ApplyInfo() {}

    public ApplyInfo(String applyId, String projectId, List<ResourceDelta> resourceDeltas) {
        this.applyId = applyId;
        this.tenantId = projectId;
        this.projectId = projectId;
        this.resourceDeltas = resourceDeltas;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<ResourceDelta> getResourceDeltas() {
        return resourceDeltas;
    }

    public void setResourceDeltas(List<ResourceDelta> resourceDeltas) {
        this.resourceDeltas = resourceDeltas;
    }
}
