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

package com.futurewei.alcor.subnet.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class SubnetRepository {

    private static final Logger logger = LoggerFactory.getLogger();

    private static final String KEY = "SubnetState";

    private ICache<String, String> subnetIdProjectIdCache;
    private CacheFactory cacheFactory;
    private Map<String, ICache<String, SubnetEntity>> subnetCache;

    @Autowired
    public SubnetRepository (CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        subnetIdProjectIdCache = cacheFactory.getCache(String.class);
        subnetCache = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SubnetRepository init completed");
    }

    @DurationStatistics
    public SubnetEntity findItem(String id) throws CacheException {
        String projectId = subnetIdProjectIdCache.get(id);
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }

        return subnetCache.get(projectId).get(id);
    }

    @DurationStatistics
    public Map<String, SubnetEntity> findAllItems(String projectId) throws CacheException {
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }
        return subnetCache.get(projectId).getAll();
    }

    @DurationStatistics
    public Map<String, SubnetEntity> findAllItems(String projectId, Map<String, Object[]> queryParams) throws CacheException {
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }
        return subnetCache.get(projectId).getAll(queryParams);
    }

    @DurationStatistics
    public void addItem(SubnetEntity subnet) throws CacheException {
        logger.log(Level.INFO, "Add subnet, subnet Id:" + subnet.getId());
        String projectId = subnet.getProjectId();
        subnetIdProjectIdCache.put(subnet.getId(), projectId);
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }
        subnetCache.get(projectId).put(subnet.getId(), subnet);
    }

    @DurationStatistics
    public void addItems(List<SubnetEntity> items) throws CacheException {
        logger.log(Level.INFO, "Add subnet batch: {}",items);
        items.forEach(item -> {
            try {
                subnetIdProjectIdCache.put(item.getId(), item.getProjectId());
            } catch (CacheException e) {
                e.printStackTrace();
            }
        });
        String projectId = items.get(0).getProjectId();
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }
        Map<String, SubnetEntity> subnetEntityMap = items.stream().collect(Collectors.toMap(SubnetEntity::getId, Function.identity()));
        subnetCache.get(projectId).putAll(subnetEntityMap);
    }

    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete subnet, subnet Id:" + id);
        String projectId = subnetIdProjectIdCache.get(id);
        if (!subnetCache.containsKey(projectId)) {
            CacheConfiguration cfg = CommonUtil.getCacheConfiguration(projectId);
            ICache<String, SubnetEntity> cache = cacheFactory.getCache(SubnetEntity.class, cfg);
            subnetCache.put(projectId, cache);
        }
        subnetCache.get(projectId).remove(id);
    }
}
