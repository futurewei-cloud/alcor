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
import java.util.stream.Collectors;

@Repository
public class IpAddrRangeRepo implements ICacheRepository<IpAddrRange> {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrRangeRepo.class);
    private static final String IP_ADDR_CACHE_NAME_PREFIX = "IpAddrCache-";

    private ICache<String, IpAddrRange> ipAddrRangeCache;
    private ICache<String, VpcIpRange> vpcIpRangeCache;
    private CacheFactory cacheFactory;

    @Autowired
    public IpAddrRangeRepo(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        ipAddrRangeCache = cacheFactory.getCache(IpAddrRange.class);
        vpcIpRangeCache = cacheFactory.getCache(VpcIpRange.class);
    }

    public IpAddrRangeRepo(ICache<String, IpAddrRange> ipAddrRangeCache,
                           ICache<String, VpcIpRange> vpcIpRangeCache) {
        this.ipAddrRangeCache = ipAddrRangeCache;
        this.vpcIpRangeCache = vpcIpRangeCache;
    }

    private String getIpAddrCacheName(String suffix) {
        return IP_ADDR_CACHE_NAME_PREFIX + suffix;
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

    private IpAddrAlloc doAllocateIpAddr(String vpcId, int ipVersion, String ipAddr) throws Exception {
        VpcIpRange vpcIpRange = vpcIpRangeCache.get(vpcId);
        if (vpcIpRange == null) {
            throw new NotFoundIpRangeFromVpc();
        }

        IpAddrAlloc ipAddrAlloc = null;
        for (String rangeId: vpcIpRange.getRanges()) {
            if (ipAddrAlloc != null) {
                break;
            }

            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            if (ipAddrRange.getIpVersion() != ipVersion) {
                continue;
            }

            try {
                ICache<String, IpAddrAlloc> ipAddrCache =
                        cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(rangeId));
                ipAddrAlloc = ipAddrRange.allocate(ipAddrCache, ipAddr);
            } catch (Exception e) {
                LOG.warn("Allocate ip address from {} failed", ipAddrRange.getId());
                continue;
            }

            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
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
                ipAddrAlloc = doAllocateIpAddr(request.getVpcId(), request.getIpVersion(), request.getIp());
            } else {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(request.getRangeId());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                ICache<String, IpAddrAlloc> ipAddrCache =
                        cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(request.getRangeId()));

                ipAddrAlloc = ipAddrRange.allocate(ipAddrCache, request.getIp());
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
    @Deprecated
    public synchronized Map<String, List<IpAddrAlloc>> allocateIpAddrBulk(Map<String, Integer> requests) throws Exception {
        Map<String, List<IpAddrAlloc>> result = new HashMap<>();

        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            for (Map.Entry<String, Integer> entry: requests.entrySet()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(entry.getKey());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                ICache<String, IpAddrAlloc> ipAddrCache =
                        cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

                List<IpAddrAlloc> ipAddrAllocs = ipAddrRange.allocateBulk(ipAddrCache, entry.getValue());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

                result.put(entry.getKey(), ipAddrAllocs);
            }

            tx.commit();
        }

        return result;
    }

    private List<IpAddrAlloc> doAllocateIpAddr(String rangeId, List<IpAddrRequest> ipRequests) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        ICache<String, IpAddrAlloc> ipAddrCache =
                cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(rangeId));

        List<String> ips = ipRequests.stream()
                .map(IpAddrRequest::getIp)
                .collect(Collectors.toList());

        List<IpAddrAlloc> result = ipAddrRange.allocateBulk(ipAddrCache, ips);
        ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

        return result;
    }

    private List<IpAddrAlloc> doAllocateIpAddr(String vpcId, int ipVersion, List<String> ips) throws Exception {
        List<IpAddrAlloc> result = new ArrayList<>();

        VpcIpRange vpcIpRange = vpcIpRangeCache.get(vpcId);
        if (vpcIpRange == null) {
            throw new NotFoundIpRangeFromVpc();
        }

        List<String> requestIps = ips.subList(0, ips.size());
        for (String rangeId: vpcIpRange.getRanges()) {
            if (result.size() == ips.size()) {
                break;
            }

            IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            if (ipAddrRange.getIpVersion() != ipVersion) {
                continue;
            }


            ICache<String, IpAddrAlloc> ipAddrCache =
                    cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(rangeId));
            List<IpAddrAlloc> ipAddrAllocs = ipAddrRange.allocateBulk(ipAddrCache, requestIps);


            if (ipAddrAllocs.size() > 0) {
                result.addAll(ipAddrAllocs);
                requestIps = ips.subList(result.size(), ips.size());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
            }
        }

        if (result.size() == 0) {
            throw new IpAddrNotEnoughException();
        }

        return result;
    }

    @DurationStatistics
    public synchronized List<IpAddrAlloc> allocateIpAddrBulk(Map<String, List<IpAddrRequest>> rangeRequests,
                                                             Map<String, List<IpAddrRequest>> vpcIpv4Requests,
                                                             Map<String, List<IpAddrRequest>> vpcIpv6Requests) throws Exception {
        List<IpAddrAlloc> result = new ArrayList<>();
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            for (Map.Entry<String, List<IpAddrRequest>> entry: rangeRequests.entrySet()) {
                result.addAll(doAllocateIpAddr(entry.getKey(), entry.getValue()));
            }

            for (Map.Entry<String, List<IpAddrRequest>> entry: vpcIpv4Requests.entrySet()) {
                result.addAll(doAllocateIpAddr(entry.getKey(),
                        IpVersion.IPV4.getVersion(),
                        entry.getValue().stream()
                            .map(IpAddrRequest::getIp)
                            .collect(Collectors.toList())));
            }

            for (Map.Entry<String, List<IpAddrRequest>> entry: vpcIpv6Requests.entrySet()) {
                result.addAll(doAllocateIpAddr(entry.getKey(),
                        IpVersion.IPV6.getVersion(),
                        entry.getValue().stream()
                                .map(IpAddrRequest::getIp)
                                .collect(Collectors.toList())));
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

            ICache<String, IpAddrAlloc> ipAddrCache =
                    cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

            ipAddrRange.modifyIpAddrState(ipAddrCache, ipAddr, state);
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

            ICache<String, IpAddrAlloc> ipAddrCache =
                    cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

            ipAddrRange.release(ipAddrCache, ipAddr);
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

                ICache<String, IpAddrAlloc> ipAddrCache =
                        cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

                ipAddrRange.releaseBulk(ipAddrCache, entry.getValue());
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

        ICache<String, IpAddrAlloc> ipAddrCache =
                cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

        return ipAddrRange.getIpAddr(ipAddrCache, ipAddr);
    }

    @DurationStatistics
    public synchronized Collection<IpAddrAlloc> getIpAddrBulk(String rangeId) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        ICache<String, IpAddrAlloc> ipAddrCache =
                cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(ipAddrRange.getId()));

        return ipAddrRange.getIpAddrBulk(ipAddrCache);
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

            //cacheFactory.getCache(IpAddrAlloc.class, getIpAddrCacheName(request.getId()));

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
