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

package com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.Common;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

public class DataCenterConfigLoader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${alcor.machine.config:app/config/machine.json}")
    private String machineConfigFile;

    String getPropertyFile(){
        return resourceLoader.getResource(this.machineConfigFile).getFilename();
    }

    public List<HostInfo> loadAndGetHostNodeList() {
        System.out.println("Loading node from " + this.machineConfigFile);
        return this.loadAndGetHostNodeList(this.machineConfigFile);
    }

    public List<HostInfo> loadAndGetHostNodeList(String machineConfigFilePath){

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        List<HostInfo> hostInfos = new ArrayList<>();

        try (FileReader reader = new FileReader(machineConfigFilePath))
        {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get("Hosts");

            nodeList.forEach( node -> {
                HostInfo hostNode = this.parseNodeObject((JSONObject) node);
                if(hostNode != null) hostInfos.add(hostNode);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return hostInfos;
    }

    private HostInfo parseNodeObject(JSONObject node)
    {
        String id = (String) node.get("id");
        String ip = (String) node.get("ip");
        String mac = (String) node.get("mac");
        System.out.println("Node ID:" + id + "|IP:" + ip + "|MAC:" + mac);

        byte[] ipByteArray;
        try{
            ipByteArray = Common.fromIpAddressStringToByteArray(ip);
            return new HostInfo(id, id, ipByteArray, mac);
        }
        catch (UnknownHostException e){

        }

        return null;
    }
}
