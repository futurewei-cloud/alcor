package com.futurewei.alcor.gatewaymanager.config;

import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.gatewaymanager.entity.ResourceType;
import com.futurewei.alcor.gatewaymanager.entity.StatusEnum;
import com.futurewei.alcor.web.entity.gateway.*;

import java.util.ArrayList;

public class UnitTestConfig {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String vpcId = "ae34051f-aa6c-4c75-abf5-50dc9ac99ef3";
    public static String gatewayEntityId = "1d5cda86-2b34-4c64-9795-c3f3b5128046";
    public static String gatewayDescription = "internal gateway";
    public static String gatewayName = "zeta gateway";
    public static String attachmentName = "zeta attachment-vpc";
    public static Integer vpcVni = 1233;

    public static String zgcId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
    public static String zgcName = "ZGC_test1";
    public static String portIbo = "8300";

    public static GatewayInfo buildResourceGatewayInfoPending() {
        GWAttachment gwAttachment = new GWAttachment(attachmentName, ResourceType.VPC, vpcId, gatewayEntityId, StatusEnum.AVAILABLE.getStatus(), vpcVni);
        ArrayList<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(gwAttachment.getId());
        GatewayEntity gatewayEntity = new GatewayEntity(projectId, gatewayEntityId, gatewayName, gatewayDescription,
                GatewayType.ZETA, StatusEnum.AVAILABLE.getStatus(), null, attachmentIds, null, null, null, null);
        ArrayList<GatewayEntity> gatewayEntities = new ArrayList<>();
        gatewayEntities.add(gatewayEntity);
        return new GatewayInfo(vpcId, gatewayEntities, null, StatusEnum.AVAILABLE.getStatus());
    }

    public static GatewayInfo buildResourceGatewayInfoReady() {
        GWAttachment gwAttachment = new GWAttachment(attachmentName, ResourceType.VPC, vpcId, gatewayEntityId, StatusEnum.AVAILABLE.getStatus(), vpcVni);
        ArrayList<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(gwAttachment.getId());
        GatewayEntity gatewayEntity = new GatewayEntity(projectId, gatewayEntityId, gatewayName, gatewayDescription,
                GatewayType.ZETA, StatusEnum.READY.getStatus(), null, attachmentIds, null, null, null, null);
        ArrayList<GatewayEntity> gatewayEntities = new ArrayList<>();
        gatewayEntities.add(gatewayEntity);
        return new GatewayInfo(vpcId, gatewayEntities, null, StatusEnum.AVAILABLE.getStatus());
    }

    public static GatewayInfo buildResourceGatewayInfoFailed() {
        GWAttachment gwAttachment = new GWAttachment(attachmentName, ResourceType.VPC, vpcId, gatewayEntityId, StatusEnum.AVAILABLE.getStatus(), vpcVni);
        ArrayList<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(gwAttachment.getId());
        GatewayEntity gatewayEntity = new GatewayEntity(projectId, gatewayEntityId, gatewayName, gatewayDescription,
                GatewayType.ZETA, StatusEnum.FAILED.getStatus(), null, attachmentIds, null, null, null, null);
        ArrayList<GatewayEntity> gatewayEntities = new ArrayList<>();
        gatewayEntities.add(gatewayEntity);
        return new GatewayInfo(vpcId, gatewayEntities, null, StatusEnum.AVAILABLE.getStatus());
    }

    public static VpcInfoSub buildResourceVpcInfoSub() {
        return new VpcInfoSub(vpcId, vpcVni);
    }

    public static ZetaGatewayIpJson buildResourceZetaGatewayIpJson() {
        ZetaGatewayIpJson zetaGatewayIpJson = new ZetaGatewayIpJson();
        zetaGatewayIpJson.setVpcId(vpcId);
        zetaGatewayIpJson.setVni(vpcVni);
        zetaGatewayIpJson.setZgcId(zgcId);
        zetaGatewayIpJson.setName(zgcName);
        zetaGatewayIpJson.setPortIbo(portIbo);
        ArrayList<GatewayIp> gatewayIps = new ArrayList<>();
        GatewayIp gatewayIp1 = new GatewayIp("192.168.0.87", "37.02.ff.cc.65.87");
        GatewayIp gatewayIp2 = new GatewayIp("192.168.0.88", "37.02.ff.cc.65.88");
        GatewayIp gatewayIp3 = new GatewayIp("192.168.0.89", "37.02.ff.cc.65.89");
        gatewayIps.add(gatewayIp1);
        gatewayIps.add(gatewayIp2);
        gatewayIps.add(gatewayIp3);
        zetaGatewayIpJson.setGatewayIps(gatewayIps);
        return zetaGatewayIpJson;
    }

    public static String vpcInfoJson() {
        return "{\n" +
                "    \"vpcinfo\": {\n" +
                "        \"vpc_id\": \"ae34051f-aa6c-4c75-abf5-50dc9ac99ef3\",\n" +
                "        \"vpc_vni\": 1233,\n" +
                "        \"owner\": \"3dda2801-d675-4688-a63f-dcda8d327f50\"\n" +
                "    }\n" +
                "}";
    }

    public static String updateGatewayInfo() {
        return "{\n" +
                "    \"gatewayinfo\": {\n" +
                "        \"resource_id\": \"ae34051f-aa6c-4c75-abf5-50dc9ac99ef3\",\n" +
                "        \"gateways\": [\n" +
                "            {\n" +
                "                \"type\": \"zeta\",\n" +
                "                \"status\": \"failed\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
    }
}
