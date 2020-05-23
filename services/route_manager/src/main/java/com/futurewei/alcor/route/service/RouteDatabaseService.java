package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.route.RouteWebObject;

import java.util.Map;

public interface RouteDatabaseService {

    public RouteWebObject getByRouteId (String routeId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllRoutes () throws CacheException;
    public void addRoute (RouteWebObject routeState) throws DatabasePersistenceException;
    public void deleteRoute (String id) throws CacheException;

}
