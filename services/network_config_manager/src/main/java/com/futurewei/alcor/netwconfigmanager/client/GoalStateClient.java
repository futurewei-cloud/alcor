package com.futurewei.alcor.netwconfigmanager.client;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface GoalStateClient {
    List<String> sendGoalStates(Map<String, HostGoalState> hostGoalStates) throws Exception;
}
