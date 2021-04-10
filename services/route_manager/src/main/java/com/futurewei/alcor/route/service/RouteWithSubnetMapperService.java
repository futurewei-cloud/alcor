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

package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.SubnetToRouteMapper;

import java.util.List;
import java.util.Map;

public interface RouteWithSubnetMapperService {

    public SubnetToRouteMapper getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public List<RouteEntity> getRuleBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllMappers () throws CacheException;
    public void addMapper (SubnetToRouteMapper subnetToRouteMapper) throws DatabasePersistenceException;
    public void addMapperByRouteEntity (String subnetId, RouteEntity routeEntity) throws DatabasePersistenceException;
    public void deleteMapper (String id) throws Exception;
    public void deleteMapperByRouteId (String subnetId, String routeId) throws Exception;


}
