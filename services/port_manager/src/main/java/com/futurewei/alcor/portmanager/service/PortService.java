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
package com.futurewei.alcor.portmanager.service;

import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.router.RouterSubnetUpdateInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface PortService {
    PortWebJson createPort(String projectId, PortWebJson portWebJson) throws Exception;

    PortWebBulkJson createPortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception;

    PortWebJson updatePort(String projectId, String portId, PortWebJson portWebJson) throws Exception;

    PortWebBulkJson updatePortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception;

    void deletePort(String projectId, String portId) throws Exception;

    PortWebJson getPort(String projectId, String portId) throws Exception;

    List<PortWebJson> listPort(String projectId) throws Exception;

    List<PortWebJson> listPort(String projectId, Map<String, Object[]> queryParams) throws Exception;

    RouterSubnetUpdateInfo updateNeighbors(String projectId, RouterSubnetUpdateInfo routerSubnetUpdateInfo) throws Exception;
}
