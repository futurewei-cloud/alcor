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
import com.futurewei.alcor.route.dao.RouteWithVpcMapperRepository;
import com.futurewei.alcor.route.service.RouteWithVpcMapperService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.VpcToRouteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteWithVpcMapperServiceImpl implements RouteWithVpcMapperService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteWithVpcMapperRepository routeWithVpcMapperRepository;

    @Autowired
    RouteRepository routeRepository;

    @Override
    @DurationStatistics
    public VpcToRouteMapper getByVpcId(String vpcId) {
        try {
            return this.routeWithVpcMapperRepository.findItem(vpcId);
        }catch (CacheException e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public List<RouteEntity> getRuleByVpcId(String vpcId) {

        List<RouteEntity> routes = new ArrayList<>();

        try {
            VpcToRouteMapper vpcToRouteMapper =  this.routeWithVpcMapperRepository.findItem(vpcId);
            if (vpcToRouteMapper == null) {
                return null;
            }

            List<String> routeIds = vpcToRouteMapper.getRouteIds();
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
        return this.routeWithVpcMapperRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addMapper(VpcToRouteMapper vpcToRouteMapper) throws DatabasePersistenceException {
        try {
            this.routeWithVpcMapperRepository.addItem(vpcToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void addMapperByRouteEntity(String vpcId, RouteEntity routeEntity) throws DatabasePersistenceException {
        try {
            if (routeEntity == null) {
                return;
            }
            String routeId = routeEntity.getId();
            VpcToRouteMapper vpcToRouteMapper = getByVpcId(vpcId);
            if (vpcToRouteMapper == null) {
                vpcToRouteMapper = new VpcToRouteMapper();
                vpcToRouteMapper.setVpcId(vpcId);
            }
            List<String> routeIds = vpcToRouteMapper.getRouteIds();
            if (routeIds == null) {
                routeIds = new ArrayList<>();
            }
            routeIds.add(routeId);
            vpcToRouteMapper.setRouteIds(routeIds);

            this.routeWithVpcMapperRepository.addItem(vpcToRouteMapper);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteMapper(String id) throws Exception {
        this.routeWithVpcMapperRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public void deleteMapperByRouteId(String vpcId, String routeId) throws Exception {
        if (vpcId == null || routeId == null) {
            return;
        }
        VpcToRouteMapper vpcToRouteMapper = getByVpcId(vpcId);
        if (vpcToRouteMapper == null) {
            return;
        }
        List<String> routeIds = vpcToRouteMapper.getRouteIds();
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
        vpcToRouteMapper.setRouteIds(routeIds);

        this.routeWithVpcMapperRepository.addItem(vpcToRouteMapper);
    }
}
