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

package com.futurewei.alcor.nodemanager.service;


import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NodeService {
    int getNodeInfoFromUpload(MultipartFile file) throws IOException, Exception;

    NodeInfo getNodeInfoById(String nodeId) throws ParameterNullOrEmptyException, Exception;

    List getAllNodes() throws Exception;

    List<NodeInfo> getAllNodes(Map<String, Object[]> queryParams) throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws ParameterNullOrEmptyException, Exception;

    List<NodeInfo> createNodeInfoBulk(List<NodeInfo> nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws ParameterNullOrEmptyException, InvalidDataException, Exception;

    String deleteNodeInfo(String nodeId) throws ParameterNullOrEmptyException, Exception;
 
    // TEMP: URI will be provided by the Admin but these are for testing purpose.
    static String makeUpNcmId(String hostIP, int localPort) { return "ncm_" + hostIP + "_" + String.valueOf(localPort); }
    static String makeUpNcmUri(String hostIP, int localPort) { return "ncm/" + hostIP + "/" + String.valueOf(localPort); }
}
