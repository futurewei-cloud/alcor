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
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.entity.InternalSubnetRouterMap;
import com.futurewei.alcor.dataplane.entity.InternalSubnets;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SubnetPortsCache {
    // The cache is a map(subnetId, subnetPorts)
    private ICache<String, InternalSubnetPorts> subnetPortsCache;
    private CacheFactory cacheFactory;

    @Autowired
    public SubnetPortsCache(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        subnetPortsCache = cacheFactory.getCache(InternalSubnetPorts.class);
    }

    @DurationStatistics
    public InternalSubnetPorts getSubnetPorts(String subnetId) throws CacheException {
        return subnetPortsCache.get(subnetId);
    }

    @DurationStatistics
    public Map<String, InternalSubnetPorts> getSubnetPortsByRouterId(String routerId) throws CacheException {
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = routerId;
        queryParams.put("routerId", values);
        return subnetPortsCache.getAll(queryParams);
    }

    @DurationStatistics
    public Map<String, InternalSubnetPorts> getAllSubnetPorts() throws CacheException {
        return subnetPortsCache.getAll();
    }

    @DurationStatistics
    public Map<String, InternalSubnetPorts> getAllSubnetPorts(Map<String, Object[]> queryParams) throws CacheException {
        return subnetPortsCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void addSubnetPorts(InternalSubnetPorts internalSubnetPorts) throws Exception {
        subnetPortsCache.put(internalSubnetPorts.getSubnetId(), internalSubnetPorts);
    }

    @DurationStatistics
    public Map<String, String> getInternalSubnetRouterMap(NetworkConfiguration networkConfig) {
        if (networkConfig.getInternalRouterInfos() != null) {
            Map<String, String> internalSubnetsRouterMap = networkConfig
                    .getInternalRouterInfos()
                    .stream()
                    .filter(routerInfo -> routerInfo.getRouterConfiguration().getSubnetRoutingTables().size() > 0)
                    .flatMap(routerInfo -> routerInfo.getRouterConfiguration().getSubnetRoutingTables()
                            .stream()
                            .map(routingTable -> new InternalSubnetRouterMap(routerInfo.getRouterConfiguration().getId()
                                    , routingTable.getSubnetId())))
                    .distinct()
                    .collect(Collectors.toMap(routerInfo -> routerInfo.getSubnetId(), routerInfo -> routerInfo.getRouterId()));
            return internalSubnetsRouterMap;
        }
        return new HashMap<>();
    }

    @DurationStatistics
    public void attacheRouter(Map<String, String> subnetIdRouterIdMap) {
       subnetIdRouterIdMap
            .entrySet()
            .forEach(subnetIdRouterId -> {
               InternalSubnetPorts internalSubnetPorts = null;
                 try {
                        internalSubnetPorts = subnetPortsCache.get(subnetIdRouterId.getKey());
                        internalSubnetPorts.setRouterId(subnetIdRouterId.getValue());
                        subnetPortsCache.put(subnetIdRouterId.getKey(), internalSubnetPorts);
                     } catch (CacheException e) {
                        e.printStackTrace();
                     }
       });
    }


    @DurationStatistics
    public Map<String, InternalSubnetPorts> getSubnetPorts(NetworkConfiguration networkConfig) throws CacheException {
        Map<String, String> internalSubnetsRouterMap = getInternalSubnetRouterMap(networkConfig);
        Map<String, InternalSubnetPorts> internalSubnetEntityMap = getSubnetPortsByRouterId(internalSubnetsRouterMap.values().stream().findFirst().orElse(""));

        Map<String, InternalSubnetPorts> internalSubnetPortsMap =  networkConfig
                .getSubnets()
                .stream()
                .filter(subnetEntity -> subnetEntity != null)
                .map(subnetEntity -> {
                        InternalSubnetPorts internalSubnetPorts = internalSubnetEntityMap.getOrDefault(subnetEntity.getId(), null);
                        if (internalSubnetPorts == null) {
                            internalSubnetPorts = new InternalSubnetPorts(subnetEntity.getId()
                                    ,subnetEntity.getGatewayPortDetail().getGatewayPortId()
                                    ,subnetEntity.getGatewayIp()
                                    ,subnetEntity.getGatewayPortDetail().getGatewayMacAddress()
                                    ,subnetEntity.getName()
                                    ,subnetEntity.getCidr()
                                    ,subnetEntity.getVpcId()
                                    ,subnetEntity.getTunnelId()
                                    ,subnetEntity.getDhcpEnable()
                                    ,internalSubnetsRouterMap.getOrDefault(subnetEntity.getId(), null));
                        }
                        return internalSubnetPorts;
                    }
                )
                .collect(Collectors.toMap(InternalSubnetPorts::getSubnetId, Function.identity()));

        return internalSubnetPortsMap;
    }

    @DurationStatistics
    public void updateSubnetPorts(Map<String, InternalSubnetPorts> internalSubnetPortsMap) throws Exception {
        subnetPortsCache.putAll(internalSubnetPortsMap);
    }

    @DurationStatistics
    public void updateSubnetPorts(InternalSubnetPorts internalSubnetPorts) throws Exception {
        subnetPortsCache.put(internalSubnetPorts.getSubnetId(), internalSubnetPorts);
    }

    @DurationStatistics
    public synchronized void deleteSubnetPorts(String subnetId) throws Exception {
        subnetPortsCache.remove(subnetId);
    }

    public Transaction getTransaction() {
        return subnetPortsCache.getTransaction();
    }

}
