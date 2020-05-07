package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkVlanRangeRequest;
import com.futurewei.alcor.web.entity.VlanKeyRequest;

import java.util.List;

public interface SegmentService {

    public Long addVlanEntity (String segmentId, String vlanId, String networkType) throws DatabasePersistenceException, CacheException;
    public Long addVxlanEntity (String segmentId) throws DatabasePersistenceException;
    public Long addGreEntity (String segmentId) throws DatabasePersistenceException;
    public void releaseVlanEntity (String vlanId, Long key) throws DatabasePersistenceException;
    public VlanKeyRequest allocateVlan(VlanKeyRequest request) throws Exception;
    public VlanKeyRequest releaseVlan(String networkType, String rangeId, Long key) throws Exception;
    public VlanKeyRequest getVlan(String networkType, String rangeId, Long key) throws Exception;
    public NetworkVlanRangeRequest createRange(NetworkVlanRangeRequest request) throws Exception;
    public NetworkVlanRangeRequest deleteRange(String rangeId) throws Exception;
    public NetworkVlanRangeRequest getRange(String rangeId) throws Exception;
    public List<NetworkVlanRangeRequest> listRanges();
}
