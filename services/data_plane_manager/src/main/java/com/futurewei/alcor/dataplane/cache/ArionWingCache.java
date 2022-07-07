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
package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.entity.ArionGroup;
import com.futurewei.alcor.dataplane.entity.ArionWing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class ArionWingCache {

    // arionWingCache store Arion wing meta data. key is Arion Wing hash code and value is Arion wing meta data.
    private ICache<String, ArionWing> arionWingCache;

    // arionWingGroupCache store Arion group meta data. key is Arion wing group name, value is Arion group meta data.
    private ICache<String, ArionGroup> arionWingGroupCache;
    private CacheFactory cacheFactory;

    @Autowired
    public ArionWingCache(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        arionWingCache = cacheFactory.getCache(ArionWing.class);
        arionWingGroupCache = cacheFactory.getCache(ArionGroup.class);
    }

    @DurationStatistics
    public ArionWing getArionWing (String resourceId) throws CacheException {
        return arionWingCache.get(resourceId);
    }

    @DurationStatistics
    public Collection<ArionWing> getArionWings () throws CacheException {
        return arionWingCache.getAll().values();
    }

    @DurationStatistics
    public Map<String, ArionWing> getAllSubnetPorts(Map<String, Object[]> queryParams) throws CacheException {
        return arionWingCache.getAll(queryParams);
    }

    @DurationStatistics
    public Collection<ArionWing> getArionWings (Set<String> keys) throws CacheException {
        return arionWingCache.getAll(keys).values();
    }

    @DurationStatistics
    public void insertArionWing (ArionWing arionWing) throws CacheException {
        arionWingCache.put(String.valueOf(arionWing.hashCode()), arionWing);
    }

    @DurationStatistics
    public void deleteArionWing (String resourceId) throws CacheException {
        arionWingCache.remove(resourceId);
    }

    @DurationStatistics
    public Object getArionGroup (String resourceId) throws CacheException {
        return arionWingGroupCache.get(resourceId);
    }

    @DurationStatistics
    public void insertArionGroup (String resourceId) throws CacheException {
        System.out.println("Insert arion group: " + resourceId);
        arionWingGroupCache.put(resourceId, new ArionGroup(resourceId));
    }

    @DurationStatistics
    public void deleteArionGroup (String resourceId) throws CacheException {
        arionWingGroupCache.remove(resourceId);
    }

    @DurationStatistics
        public Map<String, ArionGroup> getAllArionGroup () throws CacheException {
        return arionWingGroupCache.getAll();
    }

}
