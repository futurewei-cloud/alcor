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

package com.futurewei.alcor.portmanager.service;

import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.route.RouterUpdateInfo;
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

    RouterUpdateInfo updateL3Neighbors(String projectId, RouterUpdateInfo routerUpdateInfo) throws Exception;

    int getSubnetPortCount(String projectId, String subnetId) throws Exception;

    void updatePortStatus(IRestRequest request, NetworkConfiguration configuration, String status) throws Exception;
}
