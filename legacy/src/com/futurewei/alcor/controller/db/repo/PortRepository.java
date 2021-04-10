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


package com.futurewei.alcor.controller.db.repo;

import com.futurewei.alcor.controller.db.CacheFactory;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.PortState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(CacheFactory.class)
public class PortRepository implements ICacheRepository<PortState> {
    private static final Logger logger = LoggerFactory.getLogger();

    private ICache<String, PortState> cache;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(PortState.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "PortRepository init completed");
    }

    @Override
    public PortState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(PortState newItem) throws CacheException {
        logger.log(Level.INFO, "Port Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
