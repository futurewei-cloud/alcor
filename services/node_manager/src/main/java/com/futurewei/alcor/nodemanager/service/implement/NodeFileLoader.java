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

import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.web.entity.NodeInfo;
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

    public List<NodeInfo> getHostNodeListFromUpload(Reader reader) {
        JSONParser jsonParser = new JSONParser();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        logger.info(this.getClass().getName(), "getHostNodeListFromUpload");
        try {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get("Hosts");

            nodeList.forEach(node -> {
                NodeInfo hostNode = null;
                try {
                    hostNode = this.parseNodeObject((JSONObject) node);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (hostNode != null) nodeInfos.add(hostNode);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return nodeInfos;
    }

    private NodeInfo parseNodeObject(JSONObject nodeJson) throws InvalidDataException {
        NodeInfo node = null;
        String id = (String) nodeJson.get("id");
        String ip = (String) nodeJson.get("ip");
        String mac = (String) nodeJson.get("mac");
        String veth = (String) nodeJson.get("veth");
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
            if (message != null)
                throw new InvalidDataException(NodeManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
        } catch (Exception e) {
            throw e;
        }
        return node;
    }
}
