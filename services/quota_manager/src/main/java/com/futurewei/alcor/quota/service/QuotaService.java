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

package com.futurewei.alcor.quota.service;

import com.futurewei.alcor.quota.exception.QuotaException;
import com.futurewei.alcor.web.entity.quota.*;

import java.util.List;
import java.util.Map;

public interface QuotaService {

    Map<String, Integer> findQuotaByProjectId(String projectId) throws QuotaException;

    Map<String, QuotaUsageEntity> findQuotaDetailByProjectId(String projectId) throws QuotaException;

    Map<String, Integer> getDefault();

    List<Map<String, Object>> findAllQuotas() throws QuotaException;

    Map<String, Integer> updateQuota(String projectId, Map<String, Integer> quota) throws QuotaException;

    void deleteQuotaByProjectId(String projectId) throws QuotaException;

    ApplyInfo allocateQuota(String projectId, ApplyInfo applyInfo) throws QuotaException;

    String cancelQuota(String applyId) throws QuotaException;
}
