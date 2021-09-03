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
package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.VpcRepository;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VpcDatabaseServiceImpl implements VpcDatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    VpcRepository vpcRepository;

    @Override
    @DurationStatistics
    public VpcEntity getByVpcId(String vpcId) {
        try {
            return this.vpcRepository.findItem(vpcId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllVpcs() throws CacheException {
        return this.vpcRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public Map getAllVpcs(Map<String, Object[]> queryParams) throws CacheException {
        return this.vpcRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addVpc(VpcEntity vpcState) throws DatabasePersistenceException {
        try {
            this.vpcRepository.addItem(vpcState);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteVpc(String id) throws CacheException {
        this.vpcRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, VpcEntity> getCache() {
        return this.vpcRepository.getCache();
    }
}
