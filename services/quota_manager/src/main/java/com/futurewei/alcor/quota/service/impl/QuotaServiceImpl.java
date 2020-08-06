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

package com.futurewei.alcor.quota.service.impl;

import com.futurewei.alcor.quota.exception.QuotaException;
import com.futurewei.alcor.quota.service.QuotaService;
import com.futurewei.alcor.web.entity.quota.QuotaDetailEntity;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuotaServiceImpl implements QuotaService {

    @Override
    public QuotaEntity addQuota() throws QuotaException {
        return null;
    }

    @Override
    public QuotaEntity findQuotaByProjectId(String projectId) throws QuotaException {
        return null;
    }

    @Override
    public QuotaDetailEntity findQuotaDetailByProjectId(String projectId) throws QuotaException {
        return null;
    }

    @Override
    public QuotaEntity findQuotaDefaultByProjectId(String projectId) throws QuotaException {
        return null;
    }

    @Override
    public List<QuotaEntity> findAllQuotas() throws QuotaException {
        return null;
    }

    @Override
    public QuotaEntity updateQuota(QuotaEntity quotaEntity) throws QuotaException {
        return null;
    }

    @Override
    public void deleteQuotaByProjectId(String projectId) throws QuotaException {

    }
}
