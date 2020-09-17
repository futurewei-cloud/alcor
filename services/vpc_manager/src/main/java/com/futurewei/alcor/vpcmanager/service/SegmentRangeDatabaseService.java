package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;

import java.util.Map;

public interface SegmentRangeDatabaseService {

    public NetworkSegmentRangeEntity getBySegmentRangeId (String segmentRangeId);
    public Map getAllSegmentRanges () throws CacheException;
    public void addSegmentRange (NetworkSegmentRangeEntity segmentRange) throws DatabasePersistenceException;
    public void deleteSegmentRange (String id) throws CacheException;
    public ICache<String, NetworkSegmentRangeEntity> getCache ();

}
