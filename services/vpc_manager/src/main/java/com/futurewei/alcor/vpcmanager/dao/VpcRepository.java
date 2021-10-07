/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class VpcRepository implements IVpcRepository<VpcEntity> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, VpcEntity> getCache() {
        return cache;
    }
    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    private ICache<String, VpcEntity> cache;
    private CacheFactory cacheFactory;

    @Autowired
    public VpcRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        cache = cacheFactory.getCache(VpcEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "VpcRepository init completed");
    }

    @Override
    @DurationStatistics
    public VpcEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, VpcEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(VpcEntity vpcState) throws CacheException {
        logger.log(Level.INFO, "Add vpc, Vpc Id:" + vpcState.getId());
        cache.put(vpcState.getId(), vpcState);
    }

    @Override
    @DurationStatistics
    public void addItems(List<VpcEntity> items) throws CacheException {
        logger.log(Level.INFO, "Add vpc batch: {}",items);
        Map<String, VpcEntity> vpcEntityMap = items.stream().collect(Collectors.toMap(VpcEntity::getId, Function.identity()));
        cache.putAll(vpcEntityMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete vpc, Vpc Id:" + id);
        cache.remove(id);
    }

    @Override
    @DurationStatistics
    public Set<String> getSubnetIds(String projectId, String vpcId) throws CacheException {
        CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
        cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        ICache<String, String> subnetCache = cacheFactory.getCache(String.class, cfg);
        return subnetCache.getAll().entrySet().stream().filter(item -> item.getValue().equals(vpcId)).map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    @Override
    @DurationStatistics
    public void addSubnetId(String projectId, String vpcId, String subnetId) throws CacheException {
        CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
        ICache<String, String> subnetCache = cacheFactory.getCache(String.class, cfg);
        subnetCache.put(subnetId, vpcId);
    }

    @Override
    @DurationStatistics
    public void deleteSubnetId(String projectId, String vpcId, String subnetId) throws CacheException {
        CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
        ICache<String, String> subnetCache = cacheFactory.getCache(String.class, cfg);
        subnetCache.remove(subnetId);

    }

}
