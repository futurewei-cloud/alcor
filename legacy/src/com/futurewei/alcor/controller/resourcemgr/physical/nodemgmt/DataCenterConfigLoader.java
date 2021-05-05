/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.Common;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DataCenterConfigLoader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${alcor.machine.config:app/config/machine.json}")
    private String machineConfigFile;

    String getPropertyFile() {
        return resourceLoader.getResource(this.machineConfigFile).getFilename();
    }

    public List<HostInfo> loadAndGetHostNodeList() {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "Loading node from " + this.machineConfigFile);

        logger.entering(this.getClass().getName(), "List<HostInfo>");
        return this.loadAndGetHostNodeList(this.machineConfigFile);
    }

    public List<HostInfo> loadAndGetHostNodeList(String machineConfigFilePath) {

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        List<HostInfo> hostInfos = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger();
        logger.entering(this.getClass().getName(), "loadAndGetHostNodeList(String machineConfigFilePath)");
        try (FileReader reader = new FileReader(machineConfigFilePath)) {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get("Hosts");

            nodeList.forEach(node -> {
                HostInfo hostNode = this.parseNodeObject((JSONObject) node);
                if (hostNode != null) hostInfos.add(hostNode);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.exiting(this.getClass().getName(), "loadAndGetHostNodeList(String machineConfigFilePath)");
        return hostInfos;
    }

    private HostInfo parseNodeObject(JSONObject node) {
        String id = (String) node.get("id");
        String ip = (String) node.get("ip");
        String mac = (String) node.get("mac");
        System.out.println("Node ID:" + id + "|IP:" + ip + "|MAC:" + mac);

        byte[] ipByteArray;
        try {
            ipByteArray = Common.fromIpAddressStringToByteArray(ip);
            return new HostInfo(id, id, ipByteArray, mac);
        } catch (UnknownHostException e) {
            Logger logger = LoggerFactory.getLogger();
            logger.log(Level.SEVERE, "UnknownHostException");
        }

        return null;
    }
}
