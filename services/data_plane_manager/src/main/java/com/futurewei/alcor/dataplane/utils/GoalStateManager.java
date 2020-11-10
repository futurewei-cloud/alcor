/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class GoalStateManager {
  public static final int FORMAT_REVISION_NUMBER = 1;
  private final GoalStateHelper goalStateHelper = new GoalStateHelper();
  private final GoalStatePreparer DPMPreparer = new GoalStatePreparer();
  private final GoalStateTransformer dataPlaneGoalStateTransformer =
      new GoalStateTransformer(this);
  private final DataPlanePrinter dataPlanePrinter = new DataPlanePrinter();
  @Autowired private GoalStateService goalStateService;
  public static final Logger LOG = LoggerFactory.getLogger();

  public GoalStateHelper getGoalStateHelper() {
    return goalStateHelper;
  }

  public GoalStatePreparer getDPMPreparer() {
    return DPMPreparer;
  }
  /**
   * print dpm input msg
   *
   * @param networkConfiguration msg
   */
  public void printNetworkConfiguration(NetworkConfiguration networkConfiguration) {
    dataPlanePrinter.printNetworkConfiguration(networkConfiguration);
  }
  /**
   * transform client of dpm msg to aca protobuf format
   *
   * @param networkConfiguration msg to be transformmed
   * @return Map<String, Goalstate.GoalState>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  @Async
  public Future<Map<String, Goalstate.GoalState>> transformNorthToSouth(
      NetworkConfiguration networkConfiguration) throws RuntimeException {
    return dataPlaneGoalStateTransformer.transformNorthToSouth(networkConfiguration);
  }

  /**
   * deploy GoalState to ACA in parallel and return ACA processing result to upper layer
   *
   * @param gss bindHostIp realated goalstate
   * @param isFast is Fastpath
   * @param port is grpc port
   * @param isOvs is is ovs or mizar etc
   * @return List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  public List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
      talkToACA(Map<String, Goalstate.GoalState> gss, boolean isFast, int port, boolean isOvs) {
    return goalStateService.SendGoalStateToHosts(gss, isFast, port, isOvs);
  }
}
