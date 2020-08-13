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

public class ReservationInfo {

    @JsonProperty("reservation_id")
    private String reservationId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("resource_name")
    private String resourceName;

    private long expiration;

    private int amount;

    public ReservationInfo() {}

    public ReservationInfo(String reservationId, String tenantId, String projectId, String resourceName, long expiration, int amount) {
        this.reservationId = reservationId;
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.resourceName = resourceName;
        this.expiration = expiration;
        this.amount = amount;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
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

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
