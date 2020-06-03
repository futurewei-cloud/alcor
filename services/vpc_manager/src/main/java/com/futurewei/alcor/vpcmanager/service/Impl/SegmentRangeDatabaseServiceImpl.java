package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.vpcmanager.dao.SegmentRangeRepository;
import com.futurewei.alcor.vpcmanager.service.SegmentRangeDatabaseService;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SegmentRangeDatabaseServiceImpl implements SegmentRangeDatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SegmentRangeRepository segmentRangeRepository;

    @Override
    public NetworkSegmentRangeEntity getBySegmentRangeId(String segmentRangeId) {
        try {
            return this.segmentRangeRepository.findItem(segmentRangeId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllSegmentRanges() throws CacheException {
        return this.segmentRangeRepository.findAllItems();
    }

    @Override
    public void addSegmentRange(NetworkSegmentRangeEntity segmentRange) throws DatabasePersistenceException {
        try {
            this.segmentRangeRepository.addItem(segmentRange);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteSegmentRange(String id) throws CacheException {
        this.segmentRangeRepository.deleteItem(id);
    }

    @Override
    public ICache<String, NetworkSegmentRangeEntity> getCache() {
        return this.segmentRangeRepository.getCache();
    }
}
