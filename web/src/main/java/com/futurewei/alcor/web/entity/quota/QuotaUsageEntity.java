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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class QuotaUsageEntity {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private String projectId;

    @JsonIgnore
    private String resource;

    private int used;

    private int limit;

    private int reserved;

    public QuotaUsageEntity(){}

    public QuotaUsageEntity(String projectId, String resource, int used, int limit, int reserved){
        this.id = projectId + "_" + resource;
        this.projectId = projectId;
        this.resource = resource;
        this.used = used;
        this.limit = limit;
        this.reserved = reserved;
    }

    public QuotaUsageEntity(String id, String projectId, String resource, int used, int limit, int reserved){
        this.id = id;
        this.projectId = projectId;
        this.resource = resource;
        this.used = used;
        this.limit = limit;
        this.reserved = reserved;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
