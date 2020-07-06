package com.futurewei.alcor.common.db;

import com.futurewei.alcor.common.exception.DistributedLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("AclorTest")
public class MockDistributedLock implements IDistributedLock {
    private static final Logger LOG = LoggerFactory.getLogger(MockDistributedLock.class);

    public void lock(String lockKey) throws DistributedLockException {
        LOG.debug("MockDistributedLock lock: " + lockKey);
    }

    public void unlock(String lockKey) throws DistributedLockException {
        LOG.debug("MockDistributedLock unlock: " + lockKey);
    }

}
