/*
Copyright 2019 The Alcor Authors.

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

package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRangeRequest;
import com.futurewei.alcor.privateipmanager.exception.InternalDbOperationException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrRangeNotFoundException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrRangeExistException;
import com.futurewei.alcor.privateipmanager.exception.IpRangeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import com.futurewei.alcor.common.db.CacheException;
import javax.annotation.PostConstruct;
import java.util.*;

@ComponentScan(value="com.futurewei.alcor.common.db")
@Repository
public class IpAddrRangeRepo implements ICacheRepository<IpAddrRange> {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrRangeRepo.class);
    private ICache<String, IpAddrRange> ipAddrRangeCache;

    @Autowired
    public IpAddrRangeRepo(CacheFactory cacheFactory) {
        ipAddrRangeCache = cacheFactory.getCache(IpAddrRange.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("IpRangeRepository init done");
    }

    @Override
    public synchronized IpAddrRange findItem(String rangeId) {
        try {
            return ipAddrRangeCache.get(rangeId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository findItem() exception:", e);
        }

        return null;
    }

    @Override
    public synchronized Map<String, IpAddrRange> findAllItems() {
        try {
            return ipAddrRangeCache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public synchronized void addItem(IpAddrRange ipAddrRange) {
        LOG.error("Add ipAddrRange:{}", ipAddrRange);

        try {
            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository addItem() exception:", e);
        }
    }

    @Override
    public synchronized void deleteItem(String rangeId) {
        LOG.error("Delete rangeId:{}", rangeId);

        try {
            ipAddrRangeCache.remove(rangeId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository deleteItem() exception:", e);
        }
    }

    /**
     * Allocate a ip address from IpAddrRange repository
     * @param rangeId Assign ip addresses from this ip range
     * @return Ip address assigned from ip range
     * @throws Exception Db operation or ip address assignment exception
     */
    public synchronized String allocateIpAddr(String rangeId) throws Exception {
        String ipAddr;

        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            ipAddr = ipAddrRange.allocate();
            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

            tx.commit();
        }

        return ipAddr;
    }

    /**
     * Assign multiple ip addresses from IpAddrRange repository
     * @param requests The number of ip addresses that will be assigned from each ip range
     * @return Number of ip addresses assigned each ip range
     * @throws Exception Db operation or ip address assignment exception
     */
    public synchronized Map<String, List<String>> allocateIpAddrBulk(Map<String, Integer> requests) throws Exception {
        Map<String, List<String>> result = new HashMap<>();

        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            for (Map.Entry<String, Integer> entry: requests.entrySet()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(entry.getKey());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                List<String> ipAddrList = ipAddrRange.allocateBulk(entry.getValue());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

                result.put(entry.getKey(), ipAddrList);
            }

            tx.commit();
        }

        return result;
    }

    public synchronized void modifyIpAddrState(String rangeId, String ipAddr, String state) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            ipAddrRange.modifyIpAddrState(ipAddr, state);
            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

            tx.commit();
        }
    }

    public synchronized void releaseIpAddr(String rangeId, String ipAddr) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            ipAddrRange.release(ipAddr);
            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

            tx.commit();
        }
    }

    public synchronized void releaseIpAddrBulk(Map<String, List<String>> requests) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            for (Map.Entry<String, List<String>> entry: requests.entrySet()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(entry.getKey());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                ipAddrRange.releaseBulk(entry.getValue());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
            }

            tx.commit();
        }
    }

    public synchronized IpAddrAlloc getIpAddr(String rangeId, String ipAddr) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        return ipAddrRange.getIpAddr(ipAddr);
    }

    public synchronized Collection<IpAddrAlloc> getIpAddrBulk(String rangeId) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        return ipAddrRange.getIpAddrBulk();
    }

    public synchronized void createIpAddrRange(IpAddrRangeRequest request) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            if (ipAddrRangeCache.get(request.getId()) != null) {
                LOG.warn("Create ip address range failed: IpAddressRange already exists");
                throw new IpAddrRangeExistException();
            }

            IpAddrRange ipAddrRange = new IpAddrRange(request.getId(), request.getSubnetId(),
                    request.getIpVersion(), request.getFirstIp(), request.getLastIp());

            ipAddrRangeCache.put(request.getId(), ipAddrRange);

            ipAddrRange = ipAddrRangeCache.get(request.getId());
            if (ipAddrRange == null) {
                LOG.warn("Create ip address range failed: Internal db operation error");
                throw new InternalDbOperationException();
            }

            request.setUsedIps(ipAddrRange.getUsedIps());
            request.setTotalIps(ipAddrRange.getTotalIps());

            tx.commit();
        }
    }

    public synchronized IpAddrRange deleteIpAddrRange(String rangeId) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                LOG.warn("Delete ip address range failed: Ip address range not found");
                throw new IpAddrRangeNotFoundException();
            }

            ipAddrRangeCache.remove(rangeId);

            tx.commit();

            return ipAddrRange;
        }
    }

    public synchronized IpAddrRange getIpAddrRange(String rangeId) throws Exception {
            return ipAddrRangeCache.get(rangeId);
    }
}
