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
import com.futurewei.alcor.vpcmanager.dao.VlanRepository;
import com.futurewei.alcor.vpcmanager.service.VlanDatabaseService;
import com.futurewei.alcor.vpcmanager.entity.NetworkVlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VlanDatabaseServiceImpl implements VlanDatabaseService {

    @Autowired
    VlanRepository vlanRepository;

    @Override
    @DurationStatistics
    public NetworkVlanType getByVlanId(String vlanId) {
        try {
            return this.vlanRepository.findItem(vlanId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllVlans() throws CacheException {
        return this.vlanRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addVlan(NetworkVlanType vlan) throws DatabasePersistenceException {
        try {
            this.vlanRepository.addItem(vlan);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteVlan(String id) throws CacheException {
        this.vlanRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, NetworkVlanType> getCache() {
        return this.vlanRepository.getCache();
    }
}
