package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class PortHostInfoCache {


    private ICache<String, PortHostInfo> portHostInfoCache;
    private CacheFactory cacheFactory;

    @Autowired
    public PortHostInfoCache(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        portHostInfoCache = cacheFactory.getCache(PortHostInfo.class);
    }

    public Map<String, PortHostInfo> getPortHostInfo(NetworkConfiguration networkConfig) {
        Map<String, PortHostInfo> portHostInfoMap =  networkConfig
                .getPortEntities()
                .stream()
                .filter(portEntity -> portEntity.getFixedIps().size() > 0)
                .flatMap(portEntity -> portEntity.getFixedIps()
                        .stream()
                        .filter(fixedIp -> fixedIp != null)
                        .map(fixedIp -> new PortHostInfo(portEntity.getId()
                                , fixedIp.getIpAddress()
                                , portEntity.getMacAddress()
                                , portEntity.getBindingHostId()
                                , portEntity.getBindingHostIP()
                                , fixedIp.getSubnetId())))
                .collect(Collectors.toMap(portHostInfo -> portHostInfo.getSubnetId() + cacheFactory.KEY_DELIMITER + portHostInfo.getPortIp(), Function.identity()));
        return portHostInfoMap;
    }

    public void updatePortHostInfo(Map<String, PortHostInfo> portHostInfoMap) throws CacheException {
        portHostInfoCache.putAll(portHostInfoMap);
    }

    public synchronized Collection<PortHostInfo> getPortHostInfos(String subnetId) throws CacheException {
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = subnetId;
        queryParams.put("subnetId", values);
        return portHostInfoCache.getAll(queryParams).values();
    }

    public synchronized PortHostInfo getPortHostInfoByIp(String subnetId, String ip) throws CacheException {
        return portHostInfoCache.get(subnetId + cacheFactory.KEY_DELIMITER + ip);
    }

    public synchronized void deletePortHostInfo(String subnetId, String portIp) throws CacheException {
        portHostInfoCache.remove(subnetId + cacheFactory.KEY_DELIMITER + portIp);
    }

}
