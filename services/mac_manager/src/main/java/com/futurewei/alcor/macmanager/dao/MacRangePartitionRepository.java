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
package com.futurewei.alcor.macmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.mac.MacRangePartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class MacRangePartitionRepository implements ICacheRepository<MacRangePartition> {

    private final ICache<String, MacRangePartition> cache;

    @Autowired
    public MacRangePartitionRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(MacRangePartition.class);
    }

    @Override
    @DurationStatistics
    public MacRangePartition findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, MacRangePartition> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, MacRangePartition> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(MacRangePartition macRangePartition) throws CacheException {
        cache.put(macRangePartition.getId(), macRangePartition);
    }

    @Override
    @DurationStatistics
    public void addItems(List<MacRangePartition> items) throws CacheException {
        Map<String, MacRangePartition> macRangePartitionMap = items.stream().collect(Collectors.toMap(MacRangePartition::getId, Function.identity()));
        cache.putAll(macRangePartitionMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
