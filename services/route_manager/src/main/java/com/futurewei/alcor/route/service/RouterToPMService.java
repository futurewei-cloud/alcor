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
package com.futurewei.alcor.route.service;

import com.futurewei.alcor.route.exception.PortWebBulkJsonOrPortEntitiesListIsNull;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;

public interface RouterToPMService {

    public List<String> getSubnetIdsFromPM (String projectid, List<String> gatewayPorts) throws PortWebBulkJsonOrPortEntitiesListIsNull;
    public void updatePort (String projectid, String portId, PortEntity portEntity);
    public void updateL3Neighbors (String projectid, String vpcId, String subnetId, String operationType, List<String> gatewayPorts);

}
