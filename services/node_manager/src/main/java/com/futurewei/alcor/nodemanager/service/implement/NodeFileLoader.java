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

package com.futurewei.alcor.nodemanager.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class NodeFileLoader {
    private static final Logger logger = LoggerFactory.getLogger(NodeFileLoader.class);

    public NodeFileLoader() {
    }

    /**
     * make nodes' list from uploaded node file
     *
     * @param reader file reader
     * @return total nodes number
     * @throws FileNotFoundException invalid file name, IOException file read exception, Parseexception json parsing exception
     */
    @DurationStatistics
    public List<NodeInfo> getHostNodeListFromUpload(Reader reader) throws FileNotFoundException, IOException, ParseException{
        String strMethodName = "getHostNodeListFromUpload";
        JSONParser jsonParser = new JSONParser();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        logger.info(this.getClass().getName(), strMethodName);
        try {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get(NodeManagerConstant.JSON_HOSTS);
            if(nodeList != null){
                nodeList.forEach(node -> {
                    NodeInfo hostNode = null;
                    try {
                        hostNode = this.parseNodeObject((JSONObject) node);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (hostNode != null) nodeInfos.add(hostNode);
                });
            }
        } catch (FileNotFoundException e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        } catch (ParseException e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return nodeInfos;
    }

    /**
     * parse json file and create a new NodeInfo object
     *
     * @param nodeJson json format input data
     * @return NodeInfo objects
     * @throws InvalidDataException invalid json data
     */
    private NodeInfo parseNodeObject(JSONObject nodeJson) throws InvalidDataException {
        String strMethodName = "parseNodeObject";
        NodeInfo node = null;
        String id = (String) nodeJson.get(NodeManagerConstant.JSON_ID1);
        String name = (String)nodeJson.get(NodeManagerConstant.JSON_NAME);
        String ip = (String) nodeJson.get(NodeManagerConstant.JSON_IP1);
        String mac = (String) nodeJson.get(NodeManagerConstant.JSON_MAC1);
        String veth = (String) nodeJson.get(NodeManagerConstant.JSON_VETH1);
        int gRPCServerPort = NodeManagerConstant.GRPC_SERVER_PORT;
        String unicastTopic = (String) nodeJson.get(NodeManagerConstant.UNICAST_TOPIC);
        String multicastTopic = (String) nodeJson.get(NodeManagerConstant.MULTICAST_TOPIC);
        String groupTopic = (String) nodeJson.get(NodeManagerConstant.GROUP_TOPIC);
        try {
            node = new NodeInfo(id, name, ip, mac, veth, gRPCServerPort, unicastTopic, multicastTopic, groupTopic);
            String message = "";
            if (node.validateIp(ip) == false)
                message = NodeManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID;
            if (node.validateMac(mac) == false) ;
            {
                if (message != null)
                    message.concat(" & ");
                message.concat(NodeManagerConstant.NODE_EXCEPTION_MAC_FORMAT_INVALID);
            }
            if (message != null && !message.isEmpty()){
                logger.error(strMethodName+NodeManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
                throw new InvalidDataException(NodeManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
            }
            String ncm_id = (String)nodeJson.get(NodeManagerConstant.JSON_NCM_ID);
            if (ncm_id == null)
                ncm_id = NodeService.makeUpNcmId(ip, NodeManagerConstant.GRPC_SERVER_PORT);
            node.setNcmId(ncm_id);
        } catch (Exception e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return node;
    }
}