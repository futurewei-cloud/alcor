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
import com.futurewei.alcor.vpcmanager.dao.GreRepository;
import com.futurewei.alcor.vpcmanager.service.GreDatabaseService;
import com.futurewei.alcor.vpcmanager.entity.NetworkGREType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GreDatabaseServiceImpl implements GreDatabaseService {

    @Autowired
    GreRepository greRepository;

    @Override
    @DurationStatistics
    public NetworkGREType getByGreId(String greId) {
        try {
            return this.greRepository.findItem(greId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllGres() throws CacheException {
        return this.greRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addGre(NetworkGREType gre) throws DatabasePersistenceException {
        try {
            this.greRepository.addItem(gre);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteGre(String id) throws CacheException {
        this.greRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, NetworkGREType> getCache() {
        return this.greRepository.getCache();
    }
}
