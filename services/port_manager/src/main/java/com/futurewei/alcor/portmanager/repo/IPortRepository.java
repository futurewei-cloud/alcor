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
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;
import java.util.Map;

public interface IPortRepository {
    void addPortEntities(List<PortEntity> portEntities) throws CacheException;

    PortEntity findPortEntity(String portId) throws CacheException;

    Map<String, PortEntity> findAllPortEntities() throws CacheException;

    Map<String, PortEntity> findAllPortEntities(Map<String, Object[]> queryParams) throws CacheException;

    void createPortBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception;

    void updatePort(PortEntity oldPortEntity, PortEntity newPortEntity, List<NeighborInfo> neighborInfos) throws Exception;

    void deletePort(PortEntity portEntity) throws Exception;

    Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException;

    int getSubnetPortCount(String subnetId) throws CacheException;
}
