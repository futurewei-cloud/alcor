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
package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.route.RouterExtraAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class RouterExtraAttributeRepository implements ICacheRepository<RouterExtraAttribute> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouterExtraAttribute> getCache() {
        return cache;
    }

    private ICache<String, RouterExtraAttribute> cache;

    @Autowired
    public RouterExtraAttributeRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouterExtraAttribute.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouterExtraAttributeRepository init completed");
    }

    @Override
    @DurationStatistics
    public RouterExtraAttribute findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, RouterExtraAttribute> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, RouterExtraAttribute> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(RouterExtraAttribute routerExtraAttribute) throws CacheException {
        cache.put(routerExtraAttribute.getId(), routerExtraAttribute);
    }

    @Override
    @DurationStatistics
    public void addItems(List<RouterExtraAttribute> items) throws CacheException {
        Map<String, RouterExtraAttribute> routerExtraAttributeMap = items.stream().collect(Collectors.toMap(RouterExtraAttribute::getId, Function.identity()));
        cache.putAll(routerExtraAttributeMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
