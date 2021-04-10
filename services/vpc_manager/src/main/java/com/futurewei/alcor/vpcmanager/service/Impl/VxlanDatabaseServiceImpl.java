/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.VxlanRepository;
import com.futurewei.alcor.vpcmanager.service.VxlanDatabaseService;
import com.futurewei.alcor.vpcmanager.entity.NetworkVxlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VxlanDatabaseServiceImpl implements VxlanDatabaseService {

    @Autowired
    VxlanRepository vxlanRepository;

    @Override
    @DurationStatistics
    public NetworkVxlanType getByVxlanId(String vxlanId) {
        try {
            return this.vxlanRepository.findItem(vxlanId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllVxlans() throws CacheException {
        return this.vxlanRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addVxlan(NetworkVxlanType vxlan) throws DatabasePersistenceException {
        try {
            this.vxlanRepository.addItem(vxlan);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteVxlan(String id) throws CacheException {
        this.vxlanRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, NetworkVxlanType> getCache() {
        return this.vxlanRepository.getCache();
    }
}
