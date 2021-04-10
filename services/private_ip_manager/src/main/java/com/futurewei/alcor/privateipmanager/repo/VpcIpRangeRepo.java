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

package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class VpcIpRangeRepo implements ICacheRepository<VpcIpRange> {
    private static final Logger LOG = LoggerFactory.getLogger(VpcIpRangeRepo.class);

    private ICache<String, VpcIpRange> vpcIpRangeCache;

    @Autowired
    public VpcIpRangeRepo(CacheFactory cacheFactory) {
        vpcIpRangeCache = cacheFactory.getCache(VpcIpRange.class);
    }


    @Override
    public VpcIpRange findItem(String id) throws CacheException {
        return null;
    }

    @Override
    public Map<String, VpcIpRange> findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map<String, VpcIpRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return vpcIpRangeCache.getAll(queryParams);
    }

    @Override
    public void addItem(VpcIpRange newItem) throws CacheException {

    }

    @Override
    public void addItems(List<VpcIpRange> items) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {

    }
}
