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
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.vpcmanager.entity.NetworkRangeRequest;
import com.futurewei.alcor.vpcmanager.entity.VlanKeyRequest;

import java.util.List;

public interface SegmentService {

    public Long addVlanEntity (String vlanId, String networkType, String vpcId, Integer mtu) throws Exception;
    public Long addVxlanEntity (String vxlanId, String networkType, String vpcId, Integer mtu) throws Exception;
    public Long addGreEntity (String greId, String networkType, String vpcId, Integer mtu) throws DatabasePersistenceException, CacheException, Exception;
    public void releaseVlanEntity (String vlanId, Long key) throws DatabasePersistenceException;
    public void releaseVxlanEntity (String vxlanId, Long key) throws DatabasePersistenceException;
    public void releaseGreEntity (String greId, Long key) throws DatabasePersistenceException;
    public VlanKeyRequest allocateVlan(VlanKeyRequest request) throws Exception;
    public VlanKeyRequest releaseVlan(String networkType, String rangeId, Long key) throws Exception;
    public VlanKeyRequest getVlan(String networkType, String rangeId, Long key) throws Exception;
    public NetworkRangeRequest createRange(NetworkRangeRequest request) throws Exception;
    public NetworkRangeRequest deleteRange(String rangeId) throws Exception;
    public NetworkRangeRequest getRange(String rangeId) throws Exception;
    public List<NetworkRangeRequest> listRanges();
    public void createDefaultNetworkTypeTable () throws Exception;
}
