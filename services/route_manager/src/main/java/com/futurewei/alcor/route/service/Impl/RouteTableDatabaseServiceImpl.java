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
import com.futurewei.alcor.route.dao.RouteTableRepository;
import com.futurewei.alcor.route.service.RouteTableDatabaseService;
import com.futurewei.alcor.web.entity.route.RouteTable;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouteTableDatabaseServiceImpl implements RouteTableDatabaseService {

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    RouteTableRepository routeTableRepository;

    @Override
    @DurationStatistics
    public RouteTable getByRouteTableId(String routeTableId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routeTableRepository.findItem(routeTableId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllRouteTables() throws CacheException {
        return this.routeTableRepository.findAllItems();
    }

    @Override
    public Map getAllRouteTables(Map<String, Object[]> queryParams) throws CacheException {
        return this.routeTableRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addRouteTable(RouteTable routeTable) throws DatabasePersistenceException {
        try {
            this.routeTableRepository.addItem(routeTable);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteRouteTable(String id) throws Exception {
        this.routeTableRepository.deleteItem(id);
    }
}
