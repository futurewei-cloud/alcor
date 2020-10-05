package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.SegmentRepository;
import com.futurewei.alcor.vpcmanager.service.SegmentDatabaseService;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SegmentDatabaseServiceImpl implements SegmentDatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SegmentRepository segmentRepository;

    @Override
    @DurationStatistics
    public SegmentEntity getBySegmentId(String segmentId) {
        try {
            return this.segmentRepository.findItem(segmentId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllSegments() throws CacheException {
        return this.segmentRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addSegment(SegmentEntity segment) throws DatabasePersistenceException {
        try {
            this.segmentRepository.addItem(segment);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteSegment(String id) throws CacheException {
        this.segmentRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, SegmentEntity> getCache() {
        return this.segmentRepository.getCache();
    }
}
