package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.route.dao.RouteRepository;
import com.futurewei.alcor.route.service.RouteDatabaseService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RouteDatabaseServiceImpl implements RouteDatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RouteRepository routeRepository;

    @Override
    public RouteEntity getByRouteId(String routeId) {
        try {
            return this.routeRepository.findItem(routeId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllRoutes() throws CacheException {
        return this.routeRepository.findAllItems();
    }

    @Override
    public void addRoute(RouteEntity routeEntityState) throws DatabasePersistenceException {
        try {
            this.routeRepository.addItem(routeEntityState);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteRoute(String id) throws CacheException {
        this.routeRepository.deleteItem(id);
    }
}
