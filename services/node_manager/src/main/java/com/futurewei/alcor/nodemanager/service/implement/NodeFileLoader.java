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

package com.futurewei.alcor.nodemanager.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
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
        String ip = (String) nodeJson.get(NodeManagerConstant.JSON_IP1);
        String mac = (String) nodeJson.get(NodeManagerConstant.JSON_MAC1);
        String veth = (String) nodeJson.get(NodeManagerConstant.JSON_VETH1);
        int gRPCServerPort = NodeManagerConstant.GRPC_SERVER_PORT;
        try {
            node = new NodeInfo(id, id, ip, mac, veth, gRPCServerPort);
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
        } catch (Exception e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return node;
    }
}
