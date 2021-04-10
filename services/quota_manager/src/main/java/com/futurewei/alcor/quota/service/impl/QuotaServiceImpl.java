/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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

    private static final String PROJECT_ID_FIELD_NAME = "project_id";
    private static final String TENANT_ID_FIELD_NAME = "tenant_id";

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
            Map<String, QuotaEntity> quotas = quotaRepository.findProjectQuotas(projectId);
            if (quotas == null || quotas.isEmpty()) {
                return defaultQuotaMap;
            }

            for (QuotaEntity quota: quotas.values()) {
                defaultQuotaMap.put(quota.getResource(), quota.getLimit());
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
            Map<String, QuotaUsageEntity> usageEntityMap = quotaUsageRepository.findProjectQuotas(projectId);
            if (usageEntityMap == null || usageEntityMap.isEmpty()) {
                throw new TenantQuotaNotFoundException(projectId);
            }
            Map<String, QuotaUsageEntity> usageResourceMap = new HashMap<>();
            for (QuotaUsageEntity quotaUsage : usageEntityMap.values()) {
                usageResourceMap.put(quotaUsage.getResource(), quotaUsage);
            }
            return usageResourceMap;
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
    public List<Map<String, Object>> findAllQuotas() throws QuotaException {
        Map<String, Map<String, Object>> allQuotas = new HashMap<>();
        try {
            Map<String, QuotaEntity> quotaMap = quotaRepository.findAllItems();
            for (QuotaEntity quota : quotaMap.values()) {
                String projectId = quota.getProjectId();
                Map<String, Object> projectQuota = allQuotas.get(projectId);
                if (projectQuota == null) {
                    Map<String, Integer> defaultMap = defaultQuota.getDefaultsCopy();
                    projectQuota = new HashMap<>(defaultMap.size() + 2);
                    Map<String, Object> finalProjectQuota = projectQuota;
                    defaultMap.forEach((k, v) -> finalProjectQuota.merge(k, v, (v1, v2) -> v1));
                    projectQuota.put(PROJECT_ID_FIELD_NAME, projectId);
                    projectQuota.put(TENANT_ID_FIELD_NAME, projectId);
                    allQuotas.put(projectId, projectQuota);
                }
                projectQuota.put(quota.getResource(), quota.getLimit());
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
                        quotaUsageRepository.addItem(quotaUsage);
                    }
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
