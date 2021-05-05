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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.route.dao.RouteRepository;
import com.futurewei.alcor.route.dao.RouteWithSubnetMapperRepository;
import com.futurewei.alcor.route.service.RouteWithSubnetMapperService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.SubnetToRouteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteWithSubnetMapperServiceImpl implements RouteWithSubnetMapperService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteWithSubnetMapperRepository routeWithSubnetMapperRepository;

    @Autowired
    RouteRepository routeRepository;

    @Override
    @DurationStatistics
    public SubnetToRouteMapper getBySubnetId(String subnetId) {
        try {
            return this.routeWithSubnetMapperRepository.findItem(subnetId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public List<RouteEntity> getRuleBySubnetId(String subnetId) {
        List<RouteEntity> routes = new ArrayList<>();

        try {
            SubnetToRouteMapper subnetToRouteMapper =  this.routeWithSubnetMapperRepository.findItem(subnetId);
            if (subnetToRouteMapper == null) {
                return null;
            }

            List<String> routeIds = subnetToRouteMapper.getRouteIds();
            for (String routeId : routeIds) {
                RouteEntity route = this.routeRepository.findItem(routeId);
                if (route != null) {
                    routes.add(route);
                }
            }

            return routes;

        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllMappers() throws CacheException {
        return this.routeWithSubnetMapperRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addMapper(SubnetToRouteMapper subnetToRouteMapper) throws DatabasePersistenceException {
        try {
            this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void addMapperByRouteEntity(String subnetId, RouteEntity routeEntity) throws DatabasePersistenceException {
        try {
            if (routeEntity == null) {
                return;
            }

            String routeId = routeEntity.getId();
            SubnetToRouteMapper subnetToRouteMapper = getBySubnetId(subnetId);
            if (subnetToRouteMapper == null) {
                subnetToRouteMapper = new SubnetToRouteMapper();
                subnetToRouteMapper.setSubnetId(subnetId);
            }
            List<String> routeIds = subnetToRouteMapper.getRouteIds();
            if (routeIds == null) {
                routeIds = new ArrayList<>();
            }
            routeIds.add(routeId);
            subnetToRouteMapper.setRouteIds(routeIds);

            this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteMapper(String id) throws Exception {
        this.routeWithSubnetMapperRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public void deleteMapperByRouteId(String subnetId, String routeId) throws Exception {
        if (subnetId == null || routeId == null) {
            return;
        }
        SubnetToRouteMapper subnetToRouteMapper = getBySubnetId(subnetId);
        if (subnetToRouteMapper == null) {
            return;
        }
        List<String> routeIds = subnetToRouteMapper.getRouteIds();
        if (routeIds == null) {
            return;
        }
        int index = -1;
        for (int i = 0; i < routeIds.size(); i ++) {
            if (routeIds.get(i).equals(routeId)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            routeIds.remove(index);
        }
        subnetToRouteMapper.setRouteIds(routeIds);

        this.routeWithSubnetMapperRepository.addItem(subnetToRouteMapper);
    }
}
