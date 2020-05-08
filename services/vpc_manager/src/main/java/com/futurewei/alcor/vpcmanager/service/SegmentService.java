package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkRangeRequest;
import com.futurewei.alcor.web.entity.VlanKeyRequest;

import java.util.List;

public interface SegmentService {

    public Long addVlanEntity (String segmentId, String vlanId, String networkType) throws Exception;
    public Long addVxlanEntity (String segmentId, String vlanId, String networkType) throws Exception;
    public Long addGreEntity (String segmentId, String vlanId, String networkType) throws DatabasePersistenceException;
    public void releaseVlanEntity (String vlanId, Long key) throws DatabasePersistenceException;
    public void releaseVxlanEntity (String vxlanId, Long key) throws DatabasePersistenceException;
    public VlanKeyRequest allocateVlan(VlanKeyRequest request) throws Exception;
    public VlanKeyRequest releaseVlan(String networkType, String rangeId, Long key) throws Exception;
    public VlanKeyRequest getVlan(String networkType, String rangeId, Long key) throws Exception;
    public NetworkRangeRequest createRange(NetworkRangeRequest request) throws Exception;
    public NetworkRangeRequest deleteRange(String rangeId) throws Exception;
    public NetworkRangeRequest getRange(String rangeId) throws Exception;
    public List<NetworkRangeRequest> listRanges();
}
