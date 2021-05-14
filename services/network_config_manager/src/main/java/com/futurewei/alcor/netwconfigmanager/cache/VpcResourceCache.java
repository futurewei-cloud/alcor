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
package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.entity.VpcResourceMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import java.util.logging.Level;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class VpcResourceCache {
    private static final Logger LOG = LoggerFactory.getLogger();

    // Map <VNI, Map<PIP, ResourceMetadata>
    private ICache<String, VpcResourceMeta> vpcResourceMetas;

    @Autowired
    public VpcResourceCache(CacheFactory cacheFactory) {
        this.vpcResourceMetas = cacheFactory.getCache(VpcResourceMeta.class);
    }

    @DurationStatistics
    public VpcResourceMeta getResourceMeta(String vni) throws Exception {
        Long sTime = System.currentTimeMillis();
        Long uMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        LOG.log(Level.INFO, "[getResourceMeta(vni)]GRM: time " + sTime + " usedmem1 " + uMem + " vni: " + vni);
        VpcResourceMeta resourceMeta = this.vpcResourceMetas.get(vni);
        uMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Long fTime = System.currentTimeMillis() - sTime;

        LOG.log(Level.INFO, "[getResourceMeta(vni)]GRM: time " + fTime + " usedmem2 " + uMem);
        return resourceMeta;
    }

    @DurationStatistics
    public void addResourceMeta(VpcResourceMeta resourceMeta) throws Exception {
        this.vpcResourceMetas.put(resourceMeta.getVni(), resourceMeta);
    }

    @DurationStatistics
    public void updateResourceMeta(VpcResourceMeta resourceMeta) throws Exception {
        this.vpcResourceMetas.put(resourceMeta.getVni(), resourceMeta);
    }

    @DurationStatistics
    public void deleteResourceMeta(String vni) throws Exception {
        this.vpcResourceMetas.remove(vni);
    }
}
