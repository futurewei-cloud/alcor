/*Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.macmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.web.entity.mac.MacAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class MacPoolRepository implements ICacheRepository<MacAddress> {
    private ICache<String, MacAddress> cache;

    @Autowired
    public MacPoolRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(MacAddress.class);
    }

    public ICache<String, MacAddress> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {

    }

    @Override
    public MacAddress findItem(String strMacAddress) throws CacheException, ResourceNotFoundException {
        if (cache.containsKey(strMacAddress))
            return cache.get(strMacAddress);
        else
            return null;
    }

    @Override
    public Map<String, MacAddress> findAllItems() throws CacheException {
        return cache.getAll();
    }

    /**
     * add a MAC address to MAC pool repository
     *
     * @param macAddress MAC address
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void addItem(MacAddress macAddress) throws CacheException {
        if (cache.containsKey(macAddress.getMacAddress()) == false) {
            try (Transaction tx = cache.getTransaction().start()) {
                cache.put(macAddress.getMacAddress(), macAddress);
                tx.commit();
            } catch (CacheException e) {
                throw e;
            } catch(Exception e1)
            {

            }
        }
    }

    /**
     * delete a MAC address from MAC pool repository
     *
     * @param strMacAddress MAC address
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void deleteItem(String strMacAddress) throws CacheException {
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(strMacAddress);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch(Exception e)
        {

        }
    }

    public synchronized String getItem() throws CacheException {
        String strMacAddress;
        try {
            long randomIndex = ThreadLocalRandom.current().nextLong(0, getSize());
            Vector<String> sa = new Vector(Arrays.asList(cache.getAll().keySet().toArray()));
            strMacAddress = sa.elementAt((int) randomIndex);
        } catch (Exception e) {
            throw e;
        }
        return strMacAddress;
    }

    public synchronized long getSize() throws CacheException {
        int nSize = 0;
        try {
            nSize = cache.getAll().size();
        } catch (Exception e) {
            throw e;
        }
        return nSize;
    }
}
