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
import com.futurewei.alcor.macmanager.entity.MacPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class MacPoolRepository implements ICacheRepository<MacPool> {
    private ICache<String, MacPool> cache;

    @Autowired
    public MacPoolRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(MacPool.class);
    }
    private static final Logger logger = LoggerFactory.getLogger(MacPoolRepository.class);
    public ICache<String, MacPool> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {

    }

    /**
     * get a MAC pool of a range
     *
     * @param rangeId MAC range id
     * @return MAC pool of the MAC range
     * @throws CacheException Db or cache operation exception
     */
    @Override
    public MacPool findItem(String rangeId) throws CacheException {
        if (cache.containsKey(rangeId))
            return cache.get(rangeId);
        else
            return null;
    }

    /**
     * get all MAC pools
     *
     * @param
     * @return map of MAC pools, <K,V> K: MAC range id, V: MAC pool
     * @throws CacheException Db or cache operation exception
     */
    @Override
    public Map<String, MacPool> findAllItems() throws CacheException {
        return cache.getAll();
    }

    /**
     * add a MAC address to MAC pool repository
     *
     * @param pool MAC address pool
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void addItem(MacPool pool) throws CacheException {
        if (cache.containsKey(pool.getRangeId()) == false) {
            try (Transaction tx = cache.getTransaction().start()) {
                cache.put(pool.getRangeId(), pool);
                tx.commit();
            } catch (CacheException e) {
                throw e;
            } catch (Exception e) {
                logger.error("MacPoolRepository addItem() exception:", e);
            }
        }
    }

    /**
     * delete a MAC address from MAC pool repository
     *
     * @param rangeId MAC range id
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void deleteItem(String rangeId) throws CacheException {
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(rangeId);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("MacPoolRepository deleteItem() exception:", e);
        }
    }

    /**
     * add a MAC address in a MAC pool
     *
     * @param rangeId    MAC range id of a MAC pool
     * @param macAddress MAC address to add a MAC pool
     * @return
     * @throws CacheException Db or cache operation exception
     */
    public void addItem(String rangeId, String macAddress) throws CacheException {
        try {
            MacPool pool = findItem(rangeId);
            HashSet<String> setMac = pool.getSetMac();
            setMac.add(macAddress);
            pool.setSetMac(setMac);
            addItem(pool);
        } catch (CacheException e) {
            throw e;
        }
    }

    /**
     * add a MAC address in a MAC pool
     *
     * @param rangeId    MAC range id of a MAC pool
     * @param hsMacAddress bulk MAC addresses to add a MAC pool
     * @return
     * @throws CacheException Db or cache operation exception
     */
    public void addItem(String rangeId, HashSet<String> hsMacAddress) throws CacheException {
        MacPool pool = null;
        try {
            pool = findItem(rangeId);
            if(pool == null)
                pool = new MacPool(rangeId, new HashSet<String>());
            HashSet<String> hsMac = pool.getSetMac();
            if (hsMacAddress != null) {
                if (hsMacAddress.size() > 0) {
                    pool.setMacAddresses(hsMacAddress);
                }
            }
        } catch (CacheException e) {
            logger.error("MacPoolRepository addItem() exception:", e);
            throw e;
        } catch (Exception e) {
            logger.error("MacPoolRepository addItem() exception:", e);
        }
        try(Transaction tx = cache.getTransaction().start()){
            cache.put(rangeId, pool);
            tx.commit();
            logger.info("MacPoolRepository addItem() {}: ", rangeId);
        } catch (CacheException e) {
            logger.error("MacPoolRepository addItem() exception:", e);
            throw e;
        } catch (Exception e) {
            logger.error("MacPoolRepository addItem() exception:", e);
        }
    }

    /**
     * delete a MAC address in a MAC pool
     *
     * @param rangeId    MAC range id of a MAC pool
     * @param macAddress MAC address to delete
     * @return
     * @throws CacheException Db or cache operation exception
     */
    public void deleteItem(String rangeId, String macAddress) throws CacheException, ResourceNotFoundException {
        try {
            MacPool pool = findItem(rangeId);
            HashSet<String> setMac = pool.getSetMac();
            setMac.remove(macAddress);
            pool.setSetMac(setMac);
            addItem(pool);
        } catch (CacheException e) {
            throw e;
        }
    }

    /**
     * find a MAC address in a MAC pool
     *
     * @param rangeId    MAC range id of a MAC pool
     * @param macAddress MAC address to delete
     * @return MAC address
     * @throws CacheException Db or cache operation exception
     */
    public String findItem(String rangeId, String macAddress) throws CacheException {
        String strMac = null;
        try {
            MacPool pool = findItem(rangeId);
            if (pool != null) {
                HashSet<String> setMac = pool.getSetMac();
                if (setMac != null) {
                    if (setMac.contains(macAddress))
                        strMac = macAddress;
                }
            }
        } catch (CacheException e) {
            throw e;
        }
        return strMac;
    }

    /**
     * pick a MAC address randomly in a MAC pool
     *
     * @param rangeId MAC range id of a MAC pool
     * @return MAC address
     * @throws CacheException Db or cache operation exception
     */
    public synchronized String getRandomItem(String rangeId) throws CacheException {
        String strMacAddress = null;
        try {
            long nSize = getSize(rangeId);
            if (nSize > 0) {
                long randomIndex = ThreadLocalRandom.current().nextLong(0, getSize(rangeId));
                MacPool pool = findItem(rangeId);
                HashSet<String> setMac = pool.getSetMac();
                Iterator<String> iter = setMac.iterator();
                int i = 0;
                while (iter.hasNext() && i <= randomIndex) {
                    strMacAddress = iter.next();
                    i++;
                }
            }

        } catch (CacheException e) {
            throw e;
        }
        return strMacAddress;
    }

    /**
     * compute the size of MAC pool of a MAC range
     *
     * @param rangeId MAC range id of a MAC pool
     * @return size
     * @throws CacheException Db or cache operation exception
     */
    public synchronized long getSize(String rangeId) throws CacheException {
        int nSize = 0;
        try {
            MacPool pool = findItem(rangeId);
            if (pool != null)
                nSize = pool.getSetMac().size();
        } catch (CacheException e) {
            throw e;
        }
        return nSize;
    }
}
