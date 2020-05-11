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

import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        byte[] ipByteArray;
        try {
            ipByteArray = fromIpAddressStringToByteArray(ip);
            return new NodeInfo(id, id, ipByteArray, mac);
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException");
        }
        return null;
    }

    public byte[] fromIpAddressStringToByteArray(String ipAddressString) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAddressString);
        byte[] bytes = ip.getAddress();
        return bytes;
    }

    public int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
