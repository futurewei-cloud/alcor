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


package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.exception.ACAFailureException;
import com.futurewei.alcor.dataplane.exception.ClientOfDPMFailureException;
import com.futurewei.alcor.dataplane.exception.DPMFailureException;
import com.futurewei.alcor.dataplane.utils.GoalStateManager;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResult;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class GSController {
  private static final Logger LOG = LoggerFactory.getLogger();

  @Autowired private Config config;
  @Autowired private GoalStateManager goalStateManager;

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the create process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @PostMapping({"/port/", "v4/port/"})
  @ResponseStatus(HttpStatus.CREATED)
  @DurationStatistics
  public InternalDPMResultList createPort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.PORT);
    return program(gs).get();
  }

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the update process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @PutMapping({"/port/", "v4/port/"})
  @DurationStatistics
  public InternalDPMResultList updatePort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setRsType(Common.ResourceType.PORT);
    gs.setOpType(Common.OperationType.UPDATE);
    return program(gs).get();
  }

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the delete process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @DeleteMapping({"/port/", "v4/port/"})
  @DurationStatistics
  public InternalDPMResultList deletePort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.PORT);
    return program(gs).get();
  }

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the create process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @PostMapping({"/subnet/", "v4/subnet/"})
  @ResponseStatus(HttpStatus.CREATED)
  @DurationStatistics
  public InternalDPMResultList createSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs).get();
  }

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the update process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @PutMapping({"/subnet/", "v4/subnet/"})
  @DurationStatistics
  public InternalDPMResultList updateSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.UPDATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs).get();
  }

  /**
   * Accept north bound calls then transfer to ACA calls in southbound
   *
   * @param gs Encapsulation of NetworkConfiguration message
   * @return RestOperationResult in String
   * @throws Exception Various exceptions that may occur during the delete process
   * @link https://github.com/haboy52581/alcor/blob/master/docs/modules/ROOT
   *     /pages/infra_services/data_plane_manager.adoc
   */
  @DeleteMapping({"/subnet/", "v4/subnet/"})
  @DurationStatistics
  public InternalDPMResultList deleteSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs).get();
  }

  // method do the real job
  @Async
  private Future<InternalDPMResultList> program(NetworkConfiguration gs) {
    // TODO: Create a verification framework for all resources
    // leave isFast as true since SB GSinfo does not have fastpath attr
    long start = System.currentTimeMillis();
    List<InternalDPMResult> result = null;
    InternalDPMResultList resultAll = new InternalDPMResultList();

    try {
      result =
          goalStateManager
              .talkToACA(
                  goalStateManager.transformNorthToSouth(gs).get(),
                  true,
                  config.getPort(),
                  Boolean.valueOf(config.getOvs()))
              .stream()
              .flatMap(Collection::stream)
              .map(
                  f -> {
                    return new InternalDPMResult(
                        f.getResourceId(),
                        null,
                        f.getOperationStatus().toString(),
                        f.getStateElapseTime(),null);
                  })
              .collect(Collectors.toList());
      resultAll.setResultMessage("Successfully Handle request !!");

    } catch (ClientOfDPMFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, e.getMessage());
      resultAll.setResultMessage("Client of DPM sending invalid payload: " + e.getMessage());
    } catch (ACAFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, e.getMessage());
      resultAll.setResultMessage("Alcor Agent Handle request failure reason: " + e.getMessage());
    } catch (DPMFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, e.getMessage());
      resultAll.setResultMessage(
          "DataPlaneManager Handle request failure reason: " + e.getMessage());
    } catch (RuntimeException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, e.getMessage());
      resultAll.setResultMessage("Failure Handle request reason: " + e.getMessage());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    long done = System.currentTimeMillis();
    resultAll.setResultList(result);
    resultAll.setOverrallTime(done - start);
    LOG.log(Level.INFO, "DPM+ACA time cost: goalState= " + gs + " time: " + (done - start) + " ms");
    return new AsyncResult<InternalDPMResultList>(resultAll);
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public GoalStateManager getGoalStateManager() {
    return goalStateManager;
  }

  public void setGoalStateManager(GoalStateManager goalStateManager) {
    this.goalStateManager = goalStateManager;
  }
}
