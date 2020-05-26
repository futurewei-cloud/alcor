package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.SegmentEntity;

import java.util.Map;

public interface SegmentDatabaseService {

    public SegmentEntity getBySegmentId (String segmentId);
    public Map getAllSegments () throws CacheException;
    public void addSegment (SegmentEntity segment) throws DatabasePersistenceException;
    public void deleteSegment (String id) throws CacheException;
    public ICache<String, SegmentEntity> getCache ();

}
