/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
  public static final int GOAL_STATE_MESSAGE_FORMAT_VERSION = 101;
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
