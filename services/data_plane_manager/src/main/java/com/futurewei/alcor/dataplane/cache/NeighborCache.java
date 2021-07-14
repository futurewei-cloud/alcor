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
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.schema.Neighbor;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class NeighborCache {
    // The cache is a map(subnetId, subnetPorts)
    private ICache<String, Neighbor.NeighborState> neighborCache;

    @Autowired
    public NeighborCache(CacheFactory cacheFactory) {
        neighborCache = cacheFactory.getCache(Neighbor.NeighborState.class);
    }

    @DurationStatistics
    public Neighbor.NeighborState getNeiborByIP(String ip) throws Exception {
        return neighborCache.get(ip);
    }

    public void setNeighborState(Neighbor.NeighborState neighborState) throws Exception {
        //TODO support multiple FixIps
        neighborCache.put(neighborState.toBuilder().getConfiguration().getFixedIps(0).getIpAddress(), neighborState);
    }
}
