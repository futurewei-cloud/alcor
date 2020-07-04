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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.message.MessageClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;

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

  @Override
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
                } catch (ExecutionException ex) {
                  ex.printStackTrace();
                }
              });
      return result;
    }
    throw new RuntimeException("protocol other than ovs is not supported for now");
  }

  private List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> doSend(
      Goalstate.GoalState goalState, boolean isFast, int port, String ip) throws InterruptedException {
    if (isFast) {
        LOG.log(Level.FINE,
                "#### " + Thread.currentThread() + " " + ip);
        GoalStateProvisionerClient goalStateProvisionerClient =
                new GoalStateProvisionerClient(ip, port);
        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> goalStateOperationStatuses =
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
