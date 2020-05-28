package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.route.RouteEntity;

import java.util.Map;

public interface RouteDatabaseService {

    public RouteEntity getByRouteId (String routeId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllRoutes () throws CacheException;
    public void addRoute (RouteEntity routeState) throws DatabasePersistenceException;
    public void deleteRoute (String id) throws CacheException;

}
