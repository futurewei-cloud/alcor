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
package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.Map;

public interface VpcDatabaseService {

    public VpcEntity getByVpcId (String vpcId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllVpcs () throws CacheException;
    public Map getAllVpcs (Map<String, Object[]> queryParams) throws CacheException;
    public void addVpc (VpcEntity vpcState) throws DatabasePersistenceException;
    public void deleteVpc (String id) throws CacheException;
    public VpcEntity deleteSubnetIdInVpc(String vpcId, String subnetId) throws ResourceNotFoundException, DatabasePersistenceException, CacheException;
    public ICache<String, VpcEntity> getCache ();

}
