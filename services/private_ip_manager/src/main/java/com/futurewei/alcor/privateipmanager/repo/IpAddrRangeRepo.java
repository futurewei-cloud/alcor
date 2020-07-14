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
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import com.futurewei.alcor.web.entity.ip.*;
import com.futurewei.alcor.privateipmanager.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import com.futurewei.alcor.common.db.CacheException;
import javax.annotation.PostConstruct;
import java.util.*;

@Repository
public class IpAddrRangeRepo implements ICacheRepository<IpAddrRange> {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrRangeRepo.class);
    private ICache<String, IpAddrRange> ipAddrRangeCache;
    private ICache<String, VpcIpRange> vpcIpRangeCache;

    @Autowired
    public IpAddrRangeRepo(CacheFactory cacheFactory) {
        ipAddrRangeCache = cacheFactory.getCache(IpAddrRange.class);
        vpcIpRangeCache = cacheFactory.getCache(VpcIpRange.class);
    }

    public IpAddrRangeRepo(ICache<String, IpAddrRange> ipAddrRangeCache,
                           ICache<String, VpcIpRange> vpcIpRangeCache) {
        this.ipAddrRangeCache = ipAddrRangeCache;
        this.vpcIpRangeCache = vpcIpRangeCache;
    }

    @PostConstruct
    private void init() {
        LOG.info("IpRangeRepository init done");
    }

    @Override
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
    public Map<String, IpAddrRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        try {
            return ipAddrRangeCache.getAll(queryParams);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    @DurationStatistics
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
    @DurationStatistics
    public synchronized void deleteItem(String rangeId) {
        LOG.error("Delete rangeId:{}", rangeId);

        try {
            ipAddrRangeCache.remove(rangeId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("IpRangeRepository deleteItem() exception:", e);
        }
    }

    private IpAddrAlloc allocateIpdAddrByVpcId(String vpcId, int ipVersion) throws Exception {
        VpcIpRange vpcIpRange = vpcIpRangeCache.get(vpcId);
        if (vpcIpRange == null) {
            throw new NotFoundIpRangeFromVpc();
        }

        IpAddrAlloc ipAddrAlloc = null;
        for (String rangeId: vpcIpRange.getRanges()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            if (ipAddrRange.getIpVersion() != ipVersion) {
                continue;
            }

            try {
                ipAddrAlloc = ipAddrRange.allocate(null);
            } catch (IpAddrNotEnoughException e) {
                continue;
            }

            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
            break;
        }

        if (ipAddrAlloc == null) {
            throw new IpAddrNotEnoughException();
        }

        return ipAddrAlloc;
    }

    /**
     * Allocate a ip address from IpAddrRange repository
     * @param request Assign ip address request
     * @return Ip address assigned from ip range
     * @throws Exception Db operation or ip address assignment exception
     */
    @DurationStatistics
    public synchronized IpAddrAlloc allocateIpAddr(IpAddrRequest request) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrAlloc ipAddrAlloc;

            if (request.getRangeId() == null) {
                ipAddrAlloc = allocateIpdAddrByVpcId(request.getVpcId(), request.getIpVersion());
            } else {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(request.getRangeId());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                ipAddrAlloc = ipAddrRange.allocate(request.getIp());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
            }

            tx.commit();

            return ipAddrAlloc;
        }
    }

    /**
     * Assign multiple ip addresses from IpAddrRange repository
     * @param requests The number of ip addresses that will be assigned from each ip range
     * @return Number of ip addresses assigned each ip range
     * @throws Exception Db operation or ip address assignment exception
     */
    @DurationStatistics
    public synchronized Map<String, List<IpAddrAlloc>> allocateIpAddrBulk(Map<String, Integer> requests) throws Exception {
        Map<String, List<IpAddrAlloc>> result = new HashMap<>();

        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            for (Map.Entry<String, Integer> entry: requests.entrySet()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(entry.getKey());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                List<IpAddrAlloc> ipAddrAllocs = ipAddrRange.allocateBulk(entry.getValue());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

                result.put(entry.getKey(), ipAddrAllocs);
            }

            tx.commit();
        }

        return result;
    }

    @DurationStatistics
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

    @DurationStatistics
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

    @DurationStatistics
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

    @DurationStatistics
    public synchronized IpAddrAlloc getIpAddr(String rangeId, String ipAddr) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        return ipAddrRange.getIpAddr(ipAddr);
    }

    @DurationStatistics
    public synchronized Collection<IpAddrAlloc> getIpAddrBulk(String rangeId) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        return ipAddrRange.getIpAddrBulk();
    }

    @DurationStatistics
    public synchronized void createIpAddrRange(IpAddrRangeRequest request) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            if (ipAddrRangeCache.get(request.getId()) != null) {
                LOG.warn("Create ip address range failed: IpAddressRange already exists");
                throw new IpAddrRangeExistException();
            }

            IpAddrRange ipAddrRange = new IpAddrRange(request.getId(), request.getVpcId(), request.getSubnetId(),
                    request.getIpVersion(), request.getFirstIp(), request.getLastIp());

            ipAddrRangeCache.put(request.getId(), ipAddrRange);

            /*
            ipAddrRange = ipAddrRangeCache.get(request.getId());
            if (ipAddrRange == null) {
                LOG.warn("Create ip address range failed: Internal db operation error");
                throw new InternalDbOperationException();
            }
             */

            VpcIpRange vpcIpRange = vpcIpRangeCache.get(request.getVpcId());
            if (vpcIpRange == null) {
                vpcIpRange = new VpcIpRange();
                List<String> ranges = new ArrayList<>();
                ranges.add(ipAddrRange.getId());

                vpcIpRange.setVpcId(ipAddrRange.getVpcId());
                vpcIpRange.setRanges(ranges);
            } else {
                vpcIpRange.getRanges().add(ipAddrRange.getId());
            }

            vpcIpRangeCache.put(vpcIpRange.getVpcId(), vpcIpRange);
            /*
            vpcIpRange = vpcIpRangeCache.get(vpcIpRange.getVpcId());
            if (vpcIpRange == null) {
                LOG.warn("Create ip address range failed: Internal db operation error");
                throw new InternalDbOperationException();
            }*/

            request.setUsedIps(ipAddrRange.getUsedIps());
            request.setTotalIps(ipAddrRange.getTotalIps());

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized IpAddrRange deleteIpAddrRange(String rangeId) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                LOG.warn("Delete ip address range failed: Ip address range not found");
                throw new IpAddrRangeNotFoundException();
            }

            ipAddrRangeCache.remove(rangeId);

            VpcIpRange vpcIpRange = vpcIpRangeCache.get(ipAddrRange.getVpcId());
            if (vpcIpRange != null) {
                vpcIpRange.getRanges().remove(ipAddrRange.getId());

                if (vpcIpRange.getRanges().size() == 0) {
                    vpcIpRangeCache.remove(vpcIpRange.getVpcId());
                } else {
                    vpcIpRangeCache.put(vpcIpRange.getVpcId(), vpcIpRange);
                }
            } else {
                LOG.warn("Can not find VpcIpRange by vpcId: {}", ipAddrRange.getVpcId());
            }

            tx.commit();

            return ipAddrRange;
        }
    }

    @DurationStatistics
    public synchronized IpAddrRange getIpAddrRange(String rangeId) throws Exception {
        return ipAddrRangeCache.get(rangeId);
    }
}
