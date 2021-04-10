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

package com.futurewei.alcor.dataplane.service.ovs;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.message.MessageClient;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.exception.DPMFailureException;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class OVSGoalStateServiceImpl implements GoalStateService {
  private static final Logger LOG = LoggerFactory.getLogger();

  private int port;
  private String ip;

  public MessageClient getKafkaClient() {
    return kafkaClient;
  }

  MessageClient kafkaClient = null;
  ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * deploy GoalState to ACA in parallel and return ACA processing result to upper layer
   *
   * @param gss bindHostIp realated goalstate
   * @param isFast is Fastpath
   * @param grpcPort is grpc port
   * @param isOvs is is ovs or mizar etc
   * @return List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  @Override
  @DurationStatistics
  public List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
      SendGoalStateToHosts(
          Map<String, Goalstate.GoalState> gss, boolean isFast, int grpcPort, boolean isOvs) {

    if (isOvs) {
      List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result =
          new ArrayList<>();

      gss.entrySet()
          .parallelStream()
          .map(
              e -> {
                return executorService.submit(
                    () -> {
                      return this.doSend(e.getValue(), isFast, grpcPort, e.getKey());
                    });
              })
          .collect(Collectors.toList())
          .forEach(
              e -> {
                try {
                  result.add(e.get());
                } catch (InterruptedException ex) {
                  ex.printStackTrace();
                  throw new DPMFailureException(ex.getMessage());
                } catch (ExecutionException ex) {
                  ex.printStackTrace();
                  throw new DPMFailureException(ex.getMessage());
                }
              });
      return result;
    }
    throw new DPMFailureException("protocol other than ovs is not supported for now");
  }

  /**
   * deploy GoalState to ACA in parallel and return ACA processing result to upper layer
   *
   * @param goalState realated goalstate
   * @param isFast is Fastpath
   * @param grpcPort is grpc port
   * @param ip hostIp
   * @return List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  private List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> doSend(
      Goalstate.GoalState goalState, boolean isFast, int grpcPort, String ip)
      throws InterruptedException {
    if (isFast) {
      LOG.log(Level.FINE, "#### " + Thread.currentThread() + " " + ip);
      GoalStateProvisionerClient goalStateProvisionerClient =
          new GoalStateProvisionerClient(ip, grpcPort);
      List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>
          goalStateOperationStatuses =
              goalStateProvisionerClient.PushNetworkResourceStates(goalState);
      goalStateProvisionerClient.shutdown();
      return goalStateOperationStatuses;
    } else {
      String topicForEndpoint = Config.PRODUCER_CLIENT_ID + ip;
      getKafkaClient().runProducer(topicForEndpoint, goalState);
      return null;
    }
  }
}
