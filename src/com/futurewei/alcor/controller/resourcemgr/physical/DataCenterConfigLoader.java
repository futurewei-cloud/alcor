package com.futurewei.alcor.controller.resourcemgr.physical;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.Common;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataCenterConfigLoader {
    public static List<HostInfo> loadAndGetHostNodeList(String machineConfigFilePath){

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        List<HostInfo> hostInfos = new ArrayList<>();

        try (FileReader reader = new FileReader(machineConfigFilePath))
        {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray nodeList = (JSONArray) obj.get("Hosts");

            nodeList.forEach( node -> {
                HostInfo hostNode = DataCenterConfigLoader.parseNodeObject((JSONObject) node);
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

    private static HostInfo parseNodeObject(JSONObject node)
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
