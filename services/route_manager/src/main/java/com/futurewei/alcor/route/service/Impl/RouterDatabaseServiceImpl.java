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

package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.dao.RouterRepository;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.web.entity.route.Router;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouterDatabaseServiceImpl implements RouterDatabaseService {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    RouterRepository routerRepository;

    @Override
    @DurationStatistics
    public Router getByRouterId(String routerId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routerRepository.findItem(routerId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllRouters() throws CacheException {
        return this.routerRepository.findAllItems();
    }

    @Override
    public Map getAllRouters(Map<String, Object[]> queryParams) throws CacheException {
        return this.routerRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addRouter(Router router) throws DatabasePersistenceException {
        try {
            this.routerRepository.addItem(router);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteRouter(String id) throws Exception {
        this.routerRepository.deleteItem(id);
    }
}
