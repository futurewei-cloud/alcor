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
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.quota.config.DefaultQuota;
import com.futurewei.alcor.quota.dao.ApplyRepository;
import com.futurewei.alcor.quota.dao.QuotaRepository;
import com.futurewei.alcor.quota.dao.QuotaUsageRepository;
import com.futurewei.alcor.quota.exception.*;
import com.futurewei.alcor.quota.service.QuotaService;
import com.futurewei.alcor.quota.utils.QuotaUtils;
import com.futurewei.alcor.web.entity.quota.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuotaServiceImpl implements QuotaService {
    private static final Logger LOG = LoggerFactory.getLogger(QuotaService.class);

    @Autowired
    private DefaultQuota defaultQuota;

    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private QuotaUsageRepository quotaUsageRepository;

    @Autowired
    private ApplyRepository applyRepository;

    private final IDistributedLock lock;

    @Autowired
    public QuotaServiceImpl(ICacheFactory cacheFactory) {
        lock = cacheFactory.getDistributedLock(QuotaUsageEntity.class);
    }

    @Override
    public Map<String, Integer> findQuotaByProjectId(String projectId) throws QuotaException {
        Map<String, Integer> defaultQuotaMap = defaultQuota.getDefaultsCopy();
        try {
            Map<String, QuotaEntity> quotaUsages = quotaRepository.findProjectQuotas(projectId);
            if (quotaUsages == null) {
                return defaultQuotaMap;
            }

            for (QuotaEntity quotaUsage: quotaUsages.values()) {
                defaultQuotaMap.put(quotaUsage.getResource(), quotaUsage.getLimit());
            }
            return defaultQuotaMap;
        } catch (CacheException e) {
            LOG.error("get quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("get quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
    }

    @Override
    public Map<String, QuotaUsageEntity> findQuotaDetailByProjectId(String projectId) throws QuotaException {
        try {
            return quotaUsageRepository.findProjectQuotas(projectId);
        } catch (CacheException e) {
            LOG.error("get quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("get quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
    }

    @Override
    public Map<String, Integer> getDefault(){
        return defaultQuota.getDefaults();
    }

    @Override
    public List<Map<String, Integer>> findAllQuotas() throws QuotaException {
        Map<String, Map<String, Integer>> allQuotas = new HashMap<>();
        try {
            Map<String, QuotaEntity> quotaMap = quotaRepository.findAllItems();
            for (QuotaEntity quotaUsage: quotaMap.values()) {
                String projectId = quotaUsage.getProjectId();
                Map<String, Integer> projectQuota = allQuotas.get(projectId);
                if (projectQuota == null) {
                    projectQuota = defaultQuota.getDefaultsCopy();
                    allQuotas.put(projectId, projectQuota);
                }
                projectQuota.put(quotaUsage.getResource(), quotaUsage.getLimit());
            }
            return new ArrayList<>(allQuotas.values());
        } catch (CacheException e) {
            LOG.error("get all quotas entity error, {}", e.getMessage());
            throw new QuotaException(String.format("get all quotas failed, reason:%s",
                    e.getMessage()));
        }
    }

    @Override
    public Map<String, Integer> updateQuota(String projectId, Map<String, Integer> quota) throws QuotaException {
        try {
            for (Map.Entry<String, Integer> quotaEntry: quota.entrySet()) {
                String resource = quotaEntry.getKey();
                Integer limit = quotaEntry.getValue();
                String id = QuotaUtils.getCombineId(projectId, resource);
                QuotaEntity quotaEntity = quotaRepository.findItem(id);
                if (quotaEntity == null) {
                    quotaEntity = new QuotaEntity(id, projectId, resource, limit);
                }
                quotaRepository.addItem(quotaEntity);
                try {
                    lock.lock(id);
                    QuotaUsageEntity quotaUsageEntity = quotaUsageRepository.findItem(id);
                    if (quotaUsageEntity == null) {
                        quotaUsageEntity = new QuotaUsageEntity(id, projectId, resource, 0, limit, 0);
                    }
                    quotaEntity.setLimit(limit);

                    quotaUsageRepository.addItem(quotaUsageEntity);
                } finally {
                    lock.unlock(id);
                }
            }
        } catch (CacheException | DistributedLockException e) {
            LOG.error("update quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("update quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
        return quota;
    }

    @Override
    public void deleteQuotaByProjectId(String projectId) throws QuotaException {
        try {
            Map<String, QuotaEntity> quotaMap = quotaRepository.findProjectQuotas(projectId);
            if (quotaMap == null || quotaMap.isEmpty()) {
                throw new TenantQuotaNotFoundException(projectId);
            }

            for (QuotaEntity quota: quotaMap.values()) {
                String id = quota.getId();
                quotaRepository.deleteItem(id);

                try {
                    lock.lock(id);
                    QuotaUsageEntity quotaUsage = quotaUsageRepository.findItem(id);
                    if (quotaUsage != null) {
                        quotaUsage.setLimit(defaultQuota.getDefaults().get(quota.getResource()));
                    }
                    quotaUsageRepository.addItem(quotaUsage);
                } finally {
                    lock.unlock(id);
                }
            }

        } catch (CacheException | DistributedLockException e) {
            LOG.error("delete quota entity error, {}", e.getMessage());
            throw new QuotaException(String.format("update quota for project %s failed, reason:%s",
                    projectId, e.getMessage()));
        }
    }

    @Override
    public ApplyInfo allocateQuota(String projectId, ApplyInfo applyInfo) throws QuotaException {
        List<ResourceDelta> deltas = applyInfo.getResourceDeltas();
        if (deltas == null || deltas.isEmpty()) {
            throw new NoApplyDeltasException();
        }

        try {
            for (ResourceDelta delta: deltas) {
                String resource = delta.getResource();
                String usageId = QuotaUtils.getCombineId(projectId, resource);

                try {
                    lock.lock(usageId);
                    QuotaUsageEntity quotaUsage = quotaUsageRepository.findItem(usageId);
                    if (quotaUsage == null) {
                        // if no quotaUsage create a new one use default quota
                        quotaUsage = new QuotaUsageEntity(projectId, resource,
                                0, defaultQuota.getDefaults().get(resource),0);
                    }

                    int limit = quotaUsage.getLimit();
                    int totalUsed = quotaUsage.getUsed() + delta.getAmount();
                    if (limit > 0 && totalUsed > limit) {
                        throw new OverLimitQuotaException(resource);
                    }
                    quotaUsage.setUsed(totalUsed);
                    quotaUsageRepository.addItem(quotaUsage);
                } finally {
                    lock.unlock(usageId);
                }
            }
            applyRepository.addItem(applyInfo);
        } catch (CacheException | DistributedLockException e) {
            LOG.error("allocate quota failed: {}", e.getMessage());
            throw new QuotaException("allocate quota failed: " + e.getMessage());
        }
        return applyInfo;
    }

    @Override
    public String cancelQuota(String applyId) throws QuotaException {
        try {
            ApplyInfo applyInfo = applyRepository.findItem(applyId);
            if (applyInfo == null) {
                throw new ApplyInfoNotFoundException();
            }

            List<ResourceDelta> deltas = applyInfo.getResourceDeltas();
            if (deltas == null || deltas.isEmpty()) {
                throw new NoApplyDeltasException();
            }

            String projectId = applyInfo.getProjectId();
            for (ResourceDelta delta: deltas) {
                String resource = delta.getResource();
                String usageId = QuotaUtils.getCombineId(projectId, resource);

                try {
                    lock.lock(usageId);
                    QuotaUsageEntity quotaUsage = quotaUsageRepository.findItem(usageId);
                    if (quotaUsage == null) {
                        // if no quotaUsage create a new one use default quota
                        throw new QuotaNotFoundException();
                    }

                    int totalUsed = quotaUsage.getUsed() - delta.getAmount();
                    quotaUsage.setUsed(totalUsed);
                    quotaUsageRepository.addItem(quotaUsage);
                } finally {
                    lock.unlock(usageId);
                }
            }
            applyRepository.deleteItem(applyId);
        } catch (CacheException | DistributedLockException e) {
            LOG.error("allocate quota failed: {}", e.getMessage());
            throw new QuotaException("allocate quota failed: " + e.getMessage());
        }
        return applyId;
    }

}
