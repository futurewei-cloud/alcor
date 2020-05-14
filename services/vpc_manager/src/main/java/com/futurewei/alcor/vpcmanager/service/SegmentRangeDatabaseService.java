package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkSegmentRangeWebResponseObject;
import com.futurewei.alcor.web.entity.SegmentWebResponseObject;

import java.util.Map;

public interface SegmentRangeDatabaseService {

    public NetworkSegmentRangeWebResponseObject getBySegmentRangeId (String segmentRangeId);
    public Map getAllSegmentRanges () throws CacheException;
    public void addSegmentRange (NetworkSegmentRangeWebResponseObject segmentRange) throws DatabasePersistenceException;
    public void deleteSegmentRange (String id) throws CacheException;
    public ICache<String, NetworkSegmentRangeWebResponseObject> getCache ();

}
