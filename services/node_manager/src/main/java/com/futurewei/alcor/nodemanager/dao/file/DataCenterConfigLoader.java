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

package com.futurewei.alcor.nodemanager.dao.file;

import com.futurewei.alcor.controller.utilities.Common;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DataCenterConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataCenterConfigLoader.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${alcor.machine.config:app/config/machine.json}")
    private String machineConfigFile;

    String getPropertyFile() {
        return resourceLoader.getResource(this.machineConfigFile).getFilename();
    }

    public List<NodeInfo> loadAndGetHostNodeList() {
        logger.info("Loading node from " + this.machineConfigFile);
        return this.loadAndGetHostNodeList(this.machineConfigFile);
    }

    public List<NodeInfo> loadAndGetHostNodeList(String machineConfigFilePath) {
        JSONParser jsonParser = new JSONParser();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        logger.info(this.getClass().getName(), "loadAndGetHostNodeList(String machineConfigFilePath)");
        try (FileReader reader = new FileReader(machineConfigFilePath)) {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get("Hosts");

            nodeList.forEach(node -> {
                NodeInfo hostNode = this.parseNodeObject((JSONObject) node);
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

    private NodeInfo parseNodeObject(JSONObject node) {
        String id = (String) node.get("id");
        String ip = (String) node.get("ip");
        String mac = (String) node.get("mac");
        System.out.println("Node ID:" + id + "|IP:" + ip + "|MAC:" + mac);

        byte[] ipByteArray;
        try {
            ipByteArray = Common.fromIpAddressStringToByteArray(ip);
            return new NodeInfo(id, id, ipByteArray, mac);
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException");
        }
        return null;
    }
}
