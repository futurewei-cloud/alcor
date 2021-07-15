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
package com.futurewei.alcor.netwconfigmanager.entity;

import com.futurewei.alcor.netwconfigmanager.service.impl.OnDemandServiceImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;

public class VpcResourceMeta {
    private static final Logger LOG = LoggerFactory.getLogger();

    private final String NEIGHBOR_POSTFIX = "_n";

    private String vni;

    // Private IP => ResourceMetadata
    private HashMap<String, ResourceMeta> resourceMetaMap;

    public VpcResourceMeta(String vni, HashMap<String, ResourceMeta> resourceMetaMap) {
        this.vni = vni;
        this.resourceMetaMap = new HashMap<>(resourceMetaMap);
    }

    public String getVni() {
        return this.vni;
    }

    public HashMap<String, ResourceMeta> getResourceMetaMap() {
        return this.resourceMetaMap;
    }

    public ResourceMeta getResourceMeta(String privateIp) {
        Long sTime = System.currentTimeMillis();
        Long uMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        LOG.log(Level.FINE, "[getResourceMeta(privateIP)] GRM: time " + sTime + " usedmem1 " + uMem);
        ResourceMeta ret = null;

        if (this.resourceMetaMap != null && this.resourceMetaMap.containsKey(privateIp)) {
            ret = this.resourceMetaMap.get(privateIp);
        }

        uMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Long fTime = System.currentTimeMillis() - sTime;

        LOG.log(Level.FINE, "[getResourceMeta(privateIP)] GRM: time " + fTime + " usedmem2 " + uMem);

        return ret;
    }

    public void setResourceMeta(String privateIP, ResourceMeta portAssociatedResourceMeta) {
        this.resourceMetaMap.put(privateIP, portAssociatedResourceMeta);
    }

    public Set<String> getNeighborIds(String sourceIp, String destinationIp, OnDemandServiceImpl.StateProvisionAlgorithm algorithm) {
        Set<String> neighborIdSet = new HashSet<>();

        if (this.resourceMetaMap == null || !this.resourceMetaMap.containsKey(sourceIp)) {
            return neighborIdSet;
        }

        if (algorithm == OnDemandServiceImpl.StateProvisionAlgorithm.Point_To_Point) {
            if (this.resourceMetaMap.containsKey(destinationIp)) {
                String neighborId = this.resourceMetaMap.get(destinationIp).getOwnerId() + NEIGHBOR_POSTFIX;
                neighborIdSet.add(neighborId);
            }
        } else if (algorithm == OnDemandServiceImpl.StateProvisionAlgorithm.Point_To_Many) {
            // TODO: implement point to many based on ML algorithm
        } else if (algorithm == OnDemandServiceImpl.StateProvisionAlgorithm.Point_To_All) {
            for (String portIp : this.resourceMetaMap.keySet()) {
                if (!portIp.equalsIgnoreCase(sourceIp)) {
                    String neighborId = this.resourceMetaMap.get(portIp).getOwnerId() + NEIGHBOR_POSTFIX;
                    neighborIdSet.add(neighborId);
                }
            }
        }

        return neighborIdSet;
    }
}
