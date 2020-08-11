package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.VpcRepository;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VpcDatabaseServiceImpl implements VpcDatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    VpcRepository vpcRepository;

    @Override
    @DurationStatistics
    public VpcEntity getByVpcId(String vpcId) {
        try {
            return this.vpcRepository.findItem(vpcId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllVpcs() throws CacheException {
        return this.vpcRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public Map getAllVpcs(Map<String, Object[]> queryParams) throws CacheException {
        return this.vpcRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addVpc(VpcEntity vpcState) throws DatabasePersistenceException {
        try {
            this.vpcRepository.addItem(vpcState);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteVpc(String id) throws CacheException {
        this.vpcRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, VpcEntity> getCache() {
        return this.vpcRepository.getCache();
    }
}
