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
package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import com.futurewei.alcor.privateipmanager.exception.*;
import com.futurewei.alcor.web.entity.ip.IpAddrRangeRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrUpdateRequest;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class IpAddrRangeRepo implements ICacheRepository<IpAddrRange> {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrRangeRepo.class);
    private static final String IP_ADDR_CACHE_NAME_PREFIX = "IpAddrCache-";

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
    public void addItems(List<IpAddrRange> items) throws CacheException {
        Map<String, IpAddrRange> ipAddrRangeMap = items.stream().collect(Collectors.toMap(IpAddrRange::getId, Function.identity()));
        ipAddrRangeCache.putAll(ipAddrRangeMap);
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
            try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                if (ipAddrRange.getIpVersion() != ipVersion) {
                    continue;
                }

                try {
                    ipAddrAlloc = ipAddrRange.allocate(ipAddr);
                } catch (Exception e) {
                    LOG.warn("Allocate ip address from {} failed", ipAddrRange.getId());
                    continue;
                }

                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
                tx.commit();
            }

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
            IpAddrAlloc ipAddrAlloc = allocateIpAddrMethod(request);
            return ipAddrAlloc;
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

                List<IpAddrAlloc> ipAddrAllocs = ipAddrRange.allocateBulk(entry.getValue());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);

                result.put(entry.getKey(), ipAddrAllocs);
            }

            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }

        return result;
    }

    private List<IpAddrAlloc> doAllocateIpAddr(String rangeId, List<IpAddrRequest> ipRequests) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        List<String> ips = ipRequests.stream()
                .map(IpAddrRequest::getIp)
                .collect(Collectors.toList());

        List<IpAddrAlloc> result = ipAddrRange.allocateBulk(ips);
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

            List<IpAddrAlloc> ipAddrAllocs = ipAddrRange.allocateBulk(requestIps);


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
            allocateIpAddrBulkMethod(rangeRequests,vpcIpv4Requests,vpcIpv6Requests,result);
            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
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
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }
    }

    @DurationStatistics
    public synchronized void releaseIpAddr(String rangeId, String ipAddr) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            releaseIpAddrMethod(rangeId,ipAddr);
            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }
    }

    @DurationStatistics
    public synchronized void releaseIpAddrBulk(SortedMap<String, List<String>> requests) throws Exception {
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            releaseIpAddrBulkMethod(requests);
            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
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
            IpAddrRange ipAddrRange = new IpAddrRange(request.getId(), request.getVpcId(), request.getSubnetId(),
                    request.getIpVersion(), request.getFirstIp(), request.getLastIp());

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

            if (ipAddrRangeCache.get(request.getId()) != null) {
                LOG.warn("Create ip address range failed: IpAddressRange already exists");
                throw new IpAddrRangeExistException();
            }
            ipAddrRangeCache.put(request.getId(), ipAddrRange);

            request.setUsedIps(ipAddrRange.getUsedIps());
            request.setTotalIps(ipAddrRange.getTotalIps());

            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }

    }

    @DurationStatistics
    public synchronized IpAddrRange deleteIpAddrRange(String rangeId, String vpcId) throws Exception {
        IpAddrRange ipAddrRange = null;
        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            VpcIpRange vpcIpRange = vpcIpRangeCache.get(vpcId);
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

            ipAddrRange = ipAddrRangeCache.get(rangeId);
            if (ipAddrRange == null) {
                LOG.warn("Delete ip address range failed: Ip address range not found");
                throw new IpAddrRangeNotFoundException();
            }

            ipAddrRangeCache.remove(rangeId);

            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }
        return  ipAddrRange;
    }

    @DurationStatistics
    public synchronized IpAddrRange getIpAddrRange(String rangeId) throws Exception {
        return ipAddrRangeCache.get(rangeId);
    }

    @DurationStatistics
    public synchronized List<IpAddrAlloc> updateIpAddr(IpAddrUpdateRequest request,SortedMap<String, List<String>> rangeToIpAddrList,Map<String, List<IpAddrRequest>> rangeRequests,
                                                       Map<String, List<IpAddrRequest>> vpcIpv4Requests,Map<String, List<IpAddrRequest>> vpcIpv6Requests) throws Exception {
        List<IpAddrAlloc> result = null;

        try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
            if (request.getOldIpAddrRequests().size() > 0) {
                if (request.getOldIpAddrRequests().size() > 1) {
                    releaseIpAddrBulkMethod(rangeToIpAddrList);
                } else {
                    releaseIpAddrMethod(request.getOldIpAddrRequests().get(0).getRangeId(), request.getOldIpAddrRequests().get(0).getIp());
                }
            }
            if (request.getNewIpAddrRequests().size() > 0) {
                result = new ArrayList<>();
                if (request.getNewIpAddrRequests().size() > 1) {
                    allocateIpAddrBulkMethod(rangeRequests, vpcIpv4Requests, vpcIpv6Requests,result);
                } else {
                    IpAddrAlloc ipAddrAlloc = allocateIpAddrMethod(request.getNewIpAddrRequests().get(0));
                    result.add(ipAddrAlloc);
                }
            }
            tx.commit();
        } catch (Exception e) {
            LOG.warn("Transaction exception: ");
            LOG.warn(e.getMessage());
        }
        return result;
    }

    private void releaseIpAddrBulkMethod(SortedMap<String, List<String>> requests) throws Exception{
        for (Map.Entry<String, List<String>> entry: requests.entrySet()) {
            IpAddrRange ipAddrRange = ipAddrRangeCache.get(entry.getKey());
            if (ipAddrRange == null) {
                throw new IpRangeNotFoundException();
            }

            ipAddrRange.releaseBulk(entry.getValue());
            ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
        }
    }

    private void releaseIpAddrMethod(String rangeId, String ipAddr) throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeCache.get(rangeId);
        if (ipAddrRange == null) {
            throw new IpRangeNotFoundException();
        }

        ipAddrRange.release(ipAddr);
        ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
    }

    private void allocateIpAddrBulkMethod(Map<String, List<IpAddrRequest>> rangeRequests,
                                          Map<String, List<IpAddrRequest>> vpcIpv4Requests,
                                          Map<String, List<IpAddrRequest>> vpcIpv6Requests, List<IpAddrAlloc> result) throws Exception {
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

    }

    private IpAddrAlloc allocateIpAddrMethod(IpAddrRequest request) throws Exception {
        IpAddrAlloc ipAddrAlloc;
        if (request.getRangeId() == null) {
            ipAddrAlloc = doAllocateIpAddr(request.getVpcId(), request.getIpVersion(), request.getIp());
        } else {
            try (Transaction tx = ipAddrRangeCache.getTransaction().start()) {
                IpAddrRange ipAddrRange = ipAddrRangeCache.get(request.getRangeId());
                if (ipAddrRange == null) {
                    throw new IpRangeNotFoundException();
                }

                ipAddrAlloc = ipAddrRange.allocate(request.getIp());
                ipAddrRangeCache.put(ipAddrRange.getId(), ipAddrRange);
                tx.commit();
            }

        }
        return ipAddrAlloc;
    }
}