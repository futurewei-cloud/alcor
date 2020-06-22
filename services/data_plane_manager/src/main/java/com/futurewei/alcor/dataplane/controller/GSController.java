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

import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.utils.GoalStateManager;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultNB;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GSController {
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
  public List<InternalDPMResultNB> createPort(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.PORT);
    return service(gs);
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
  public List<InternalDPMResultNB> updatePort(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setRsType(Common.ResourceType.PORT);
    gs.setOpType(Common.OperationType.UPDATE);
    return service(gs);
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
  public List<InternalDPMResultNB> deletePort(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.PORT);
    return service(gs);
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
  public List<InternalDPMResultNB> createSubnet(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setOpType(Common.OperationType.CREATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return service(gs);
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
  public List<InternalDPMResultNB> updateSubnet(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setOpType(Common.OperationType.UPDATE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return service(gs);
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
  public List<InternalDPMResultNB> deleteSubnet(@RequestBody NetworkConfiguration gs)
      throws Exception {
    gs.setOpType(Common.OperationType.DELETE);
    gs.setRsType(Common.ResourceType.SUBNET);
    return service(gs);
  }

  private List<InternalDPMResultNB> service(NetworkConfiguration gs) {
    // TODO: Create a verification framework for all resources
    // leave isFast as true since SB GSinfo does not have fastpath attr
    try {
      return goalStateManager
          .talkToACA(
              goalStateManager.transformNorthToSouth(gs),
              true,
              Integer.parseInt(config.getPort()),
              Boolean.valueOf(config.getOvs()))
          .stream()
          .flatMap(Collection::stream)
          .map(
              f -> {
                return new InternalDPMResultNB(
                    f.getResourceId(),
                    f.getResourceType().toString(),
                    f.getOperationStatus().toString(),
                    f.getStateElapseTime());
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      InternalDPMResultNB t =
          new InternalDPMResultNB(
              "DPM Internal Error", " please check if input to dpm is valid", "500", -1);
      List<InternalDPMResultNB> r = new ArrayList<>();
      r.add(t);
      return r;
    }
  }
}
