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
import com.futurewei.alcor.route.dao.RouteEntryRepository;
import com.futurewei.alcor.route.service.RouteEntryDatabaseService;
import com.futurewei.alcor.web.entity.route.RouteEntry;
import com.futurewei.alcor.common.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouteEntryDatabaseServiceImpl implements RouteEntryDatabaseService{

    private Logger logger = LoggerFactory.getLogger();

    @Autowired
    RouteEntryRepository routeEntryRepository;

    @Override
    @DurationStatistics
    public RouteEntry getByRouteEntryId(String routeEntryId) throws ResourceNotFoundException, ResourcePersistenceException {
        try {
            return this.routeEntryRepository.findItem(routeEntryId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllRouteEntries() throws CacheException {
        return this.routeEntryRepository.findAllItems();
    }

    @Override
    public Map getAllRouteEntries(Map<String, Object[]> queryParams) throws CacheException {
        return this.routeEntryRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addRouteEntry(RouteEntry routeEntry) throws DatabasePersistenceException {
        try {
            this.routeEntryRepository.addItem(routeEntry);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteRouteEntry(String id) throws Exception {
        this.routeEntryRepository.deleteItem(id);
    }
}
