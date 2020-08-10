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

package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
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
  public InternalDPMResultList createPort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.PORT);
    return program(gs);
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
  public InternalDPMResultList updatePort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setRsType(Common.ResourceType.PORT);
    gs.setOpType(Common.OperationType.UPDATE);
    return program(gs);
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
  public InternalDPMResultList deletePort(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.PORT);
    return program(gs);
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
  public InternalDPMResultList createSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs);
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
  public InternalDPMResultList updateSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.UPDATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs);
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
  public InternalDPMResultList deleteSubnet(@RequestBody NetworkConfiguration gs) throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return program(gs);
  }

  // method do the real job
  private InternalDPMResultList program(NetworkConfiguration gs) {
    // TODO: Create a verification framework for all resources
    // leave isFast as true since SB GSinfo does not have fastpath attr
    long start = System.currentTimeMillis();
    List<InternalDPMResult> result = null;
    InternalDPMResultList resultAll = new InternalDPMResultList();

    try {
      result =
          goalStateManager
              .talkToACA(
                  goalStateManager.transformNorthToSouth(gs),
                  true,
                  Integer.parseInt(config.getPort()),
                  Boolean.valueOf(config.getOvs()))
              .stream()
              .flatMap(Collection::stream)
              .map(
                  f -> {
                    return new InternalDPMResult(
                        f.getResourceId(),
                        f.getResourceType().toString(),
                        f.getOperationStatus().toString(),
                        f.getStateElapseTime());
                  })
              .collect(Collectors.toList());
      resultAll.setResultMessage("Successfully Handle request !!");

    } catch (ClientOfDPMFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE,e.getMessage());
      resultAll.setResultMessage("Client of DPM sending invalid payload: " + e.getMessage());
    }
    catch (ACAFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE,e.getMessage());
      resultAll.setResultMessage("Alcor Agent Handle request failure reason: " + e.getMessage());
    }
    catch (DPMFailureException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE,e.getMessage());
      resultAll.setResultMessage("DataPlaneManager Handle request failure reason: " + e.getMessage());
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE,e.getMessage());
      resultAll.setResultMessage("Failure Handle request reason: " + e.getMessage());
    }
    long done = System.currentTimeMillis();
    resultAll.setResultList(result);
    resultAll.setOverrallTime(done - start);
    LOG.log(Level.INFO,"DPM+ACA time cost: goalState= "+gs+" time: "+(done - start)+" ms");
    return resultAll;
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
