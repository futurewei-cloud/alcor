package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.SegmentWebResponseObject;
import com.futurewei.alcor.web.entity.vpc.VpcWebResponseObject;

import java.util.Map;

public interface SegmentDatabaseService {

    public SegmentWebResponseObject getBySegmentId (String segmentId);
    public Map getAllSegments () throws CacheException;
    public void addSegment (SegmentWebResponseObject segment) throws DatabasePersistenceException;
    public void deleteSegment (String id) throws CacheException;
    public ICache<String, SegmentWebResponseObject> getCache ();

}
