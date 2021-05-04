package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;

public interface GoalStatePersistenceService {

    /**
     * Update Resource Goal State in the Service distributed cache.
     *
     * @param hostGoalState
     * @return boolean
     * @throws Exception Various exceptions that may occur during the create process
     */
    boolean updateGoalState(String hostId, HostGoalState hostGoalState) throws Exception;

}
