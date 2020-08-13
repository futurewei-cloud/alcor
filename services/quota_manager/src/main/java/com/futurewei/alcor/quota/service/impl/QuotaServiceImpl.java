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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.quota.dao.QuotaRepository;
import com.futurewei.alcor.quota.exception.QuotaException;
import com.futurewei.alcor.quota.exception.QuotaNotFoundException;
import com.futurewei.alcor.quota.service.QuotaService;
import com.futurewei.alcor.web.entity.quota.QuotaDetailEntity;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import com.google.common.collect.Lists;
import org.apache.kafka.common.metrics.stats.SampledStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuotaServiceImpl implements QuotaService {
    private static final Logger LOG = LoggerFactory.getLogger(QuotaService.class);

    @Value("${quota.default.floating_ip:50}")
    private int defaultFloatingIp;

    @Value("${quota.default.network:10}")
    private int defaultNetwork;

    @Value("${quota.default.port:50}")
    private int defaultPort;

    @Value("${quota.default.rbac_policy:-1}")
    private int defaultRbacPolicy;

    @Value("${quota.default.router:10}")
    private int defaultRouter;

    @Value("${quota.default.security_group:10}")
    private int defaultSG;

    @Value("${quota.default.security_group_rule:100}")
    private int defaultSGR;

    @Value("${quota.default.subnet:10}")
    private int defaultSubnet;

    @Value("${quota.default.subnetpool:-1}")
    private int defaultSubnetPool;

    @Autowired
    private QuotaRepository quotaRepository;

    @Override
    public QuotaEntity addQuota(QuotaEntity quotaEntity) throws QuotaException {
        try {
            quotaRepository.addItem(quotaEntity);
        } catch (CacheException e) {
            LOG.error("add quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("add quota for project %s failed, reason:%s",
                    quotaEntity.getProjectId(), e.getMessage()));
        }
        return quotaEntity;
    }

    @Override
    public QuotaEntity findQuotaByProjectId(String projectId) throws QuotaException {
        try {
            QuotaEntity quotaEntity = quotaRepository.findItem(projectId);
            if (quotaEntity == null) {
                throw new QuotaNotFoundException();
            }
            return quotaEntity;
        } catch (CacheException e) {
            LOG.error("get quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("get quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
    }

    @Override
    public QuotaDetailEntity findQuotaDetailByProjectId(String projectId) throws QuotaException {
        return null;
    }

    @Override
    public QuotaEntity getDefault(){
        return new QuotaEntity(defaultFloatingIp, defaultNetwork, defaultPort, defaultRbacPolicy,
                defaultRouter, defaultSG, defaultSGR, defaultSubnet, defaultSubnetPool);
    }

    @Override
    public List<QuotaEntity> findAllQuotas() throws QuotaException {
        try {
            Map<String, QuotaEntity> quotaMap = quotaRepository.findAllItems();
            return quotaMap == null ? Collections.emptyList(): new ArrayList<>(quotaMap.values());
        } catch (CacheException e) {
            LOG.error("get all quotas entity error, {}", e.getMessage());
            throw new QuotaException(String.format("get all quotas failed, reason:%s",
                    e.getMessage()));
        }
    }

    @Override
    public QuotaEntity updateQuota(QuotaEntity quotaEntity) throws QuotaException {
        try {
            quotaRepository.addItem(quotaEntity);
        } catch (CacheException e) {
            LOG.error("update quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("update quota for project %s failed, reason:%s",
                    quotaEntity.getProjectId(), e.getMessage()));
        }
        return quotaEntity;
    }

    @Override
    public void deleteQuotaByProjectId(String projectId) throws QuotaException {
        try {
            quotaRepository.deleteItem(projectId);
        } catch (CacheException e) {
            LOG.error("delete quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("update quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
    }
}
