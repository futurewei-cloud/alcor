package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoalStateUtilTest {
  private static final Logger LOG = LoggerFactory.getLogger(GoalStateUtilTest.class);

  GoalStateUtil goalStateUtil;

  @Before
  public void setup() {
    this.goalStateUtil = new GoalStateUtil();
  }

  @After
  public void destroy() {
    this.goalStateUtil = null;
  }

  @Test
  public void transform2PortsOn1Host1Subnet1VPC1FixedIp(){
    String input =
        "{\n"
            + "\t\"rsType\": \"PORT\",\n"
            + "\t\"opType\": \"CREATE\",\n"
            + "\t\"allOrNone\": true,\n"
            + "\t\"portStates\": [{\n"
            + "\t\t\"bindingHostIP\": \"10.213.43.187\",\n"
            + "\t\t\"subnetEntities\": [{\n"
            + "\t\t\t\"tunnelId\": 88888,\n"
            + "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\t\"dhcpEnable\": false,\n"
            + "\t\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"vpcEntities\": [{\n"
            + "\t\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\t\"adminStateUp\": false,\n"
            + "\t\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\t\"routerExternal\": false,\n"
            + "\t\t\t\"shared\": false,\n"
            + "\t\t\t\"vlanTransparent\": false,\n"
            + "\t\t\t\"isDefault\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"adminStateUp\": true,\n"
            + "\t\t\"macAddress\": \"86:ea:77:ad:52:55\",\n"
            + "\t\t\"vethName\": \"veth0\",\n"
            + "\t\t\"fastPath\": true,\n"
            + "\t\t\"fixedIps\": [{\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.2\"\n"
            + "\t\t}],\n"
            + "\t\t\"bindingHostId\": \"ephost_0\",\n"
            + "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"revisionNumber\": 0,\n"
            + "\t\t\"resourceRequest\": 0,\n"
            + "\t\t\"uplinkStatusPropagation\": false,\n"
            + "\t\t\"macLearningEnabled\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n"
            + "\t\t\"name\": \"test_cni_port2\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}, {\n"
            + "\t\t\"bindingHostIP\": \"10.213.43.187\",\n"
            + "\t\t\"subnetEntities\": [{\n"
            + "\t\t\t\"tunnelId\": 88888,\n"
            + "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\t\"dhcpEnable\": false,\n"
            + "\t\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"vpcEntities\": [{\n"
            + "\t\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\t\"adminStateUp\": false,\n"
            + "\t\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\t\"routerExternal\": false,\n"
            + "\t\t\t\"shared\": false,\n"
            + "\t\t\t\"vlanTransparent\": false,\n"
            + "\t\t\t\"isDefault\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"adminStateUp\": true,\n"
            + "\t\t\"macAddress\": \"86:ea:77:ad:52:58\",\n"
            + "\t\t\"vethName\": \"veth0\",\n"
            + "\t\t\"fastPath\": true,\n"
            + "\t\t\"fixedIps\": [{\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.4\"\n"
            + "\t\t}],\n"
            + "\t\t\"bindingHostId\": \"ephost_0\",\n"
            + "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"revisionNumber\": 0,\n"
            + "\t\t\"resourceRequest\": 0,\n"
            + "\t\t\"uplinkStatusPropagation\": false,\n"
            + "\t\t\"macLearningEnabled\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n"
            + "\t\t\"name\": \"test_cni_port5\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"vpcs\": [{\n"
            + "\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\"adminStateUp\": false,\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"routerExternal\": false,\n"
            + "\t\t\"shared\": false,\n"
            + "\t\t\"vlanTransparent\": false,\n"
            + "\t\t\"isDefault\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"subnets\": [{\n"
            + "\t\t\"tunnelId\": 88888,\n"
            + "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\"dhcpEnable\": false,\n"
            + "\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"securityGroupEntities\": [{}, {}]\n"
            + "}";
    String result =
        "{\n"
            + "\t\"10.213.43.187\": {\n"
            + "\t\t\"bitField0_\": 0,\n"
            + "\t\t\"formatVersion_\": 0,\n"
            + "\t\t\"vpcStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 851612649\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1068653077\n"
            + "\t\t}],\n"
            + "\t\t\"subnetStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88888,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -572469531\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1003882173\n"
            + "\t\t}],\n"
            + "\t\t\"portStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port2\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:55\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040234\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 757879522\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": -1751579882\n"
            + "\t\t}],\n"
            + "\t\t\"securityGroupStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 0,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"securityGroupRules_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t}],\n"
            + "\t\t\"dhcpStates_\": [],\n"
            + "\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\"unknownFields\": {\n"
            + "\t\t\t\"fields\": {},\n"
            + "\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t},\n"
            + "\t\t\"memoizedSize\": -1,\n"
            + "\t\t\"memoizedHashCode\": 0\n"
            + "\t}\n"
            + "}";
    handleData(input,result);
  }
  @Test
  public void transform2PortsOn1Host1Subnet1VPC2FixedIp()
  {
    String input =
        "{\n"
            + "\t\"rsType\": \"PORT\",\n"
            + "\t\"opType\": \"CREATE\",\n"
            + "\t\"allOrNone\": true,\n"
            + "\t\"portStates\": [{\n"
            + "\t\t\"bindingHostIP\": \"10.213.43.187\",\n"
            + "\t\t\"subnetEntities\": [{\n"
            + "\t\t\t\"tunnelId\": 88888,\n"
            + "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\t\"dhcpEnable\": false,\n"
            + "\t\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"vpcEntities\": [{\n"
            + "\t\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\t\"adminStateUp\": false,\n"
            + "\t\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\t\"routerExternal\": false,\n"
            + "\t\t\t\"shared\": false,\n"
            + "\t\t\t\"vlanTransparent\": false,\n"
            + "\t\t\t\"isDefault\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"adminStateUp\": true,\n"
            + "\t\t\"macAddress\": \"86:ea:77:ad:52:55\",\n"
            + "\t\t\"vethName\": \"veth0\",\n"
            + "\t\t\"fastPath\": true,\n"
            + "\t\t\"fixedIps\": [{\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.2\"\n"
            + "\t\t}, {\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.3\"\n"
            + "\t\t}],\n"
            + "\t\t\"bindingHostId\": \"ephost_0\",\n"
            + "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"revisionNumber\": 0,\n"
            + "\t\t\"resourceRequest\": 0,\n"
            + "\t\t\"uplinkStatusPropagation\": false,\n"
            + "\t\t\"macLearningEnabled\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n"
            + "\t\t\"name\": \"test_cni_port2\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}, {\n"
            + "\t\t\"bindingHostIP\": \"10.213.43.187\",\n"
            + "\t\t\"subnetEntities\": [{\n"
            + "\t\t\t\"tunnelId\": 88888,\n"
            + "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\t\"dhcpEnable\": false,\n"
            + "\t\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"vpcEntities\": [{\n"
            + "\t\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\t\"adminStateUp\": false,\n"
            + "\t\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\t\"routerExternal\": false,\n"
            + "\t\t\t\"shared\": false,\n"
            + "\t\t\t\"vlanTransparent\": false,\n"
            + "\t\t\t\"isDefault\": false,\n"
            + "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\t\"description\": \"\"\n"
            + "\t\t}],\n"
            + "\t\t\"adminStateUp\": true,\n"
            + "\t\t\"macAddress\": \"86:ea:77:ad:52:58\",\n"
            + "\t\t\"vethName\": \"veth0\",\n"
            + "\t\t\"fastPath\": true,\n"
            + "\t\t\"fixedIps\": [{\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.4\"\n"
            + "\t\t}, {\n"
            + "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\"ipAddress\": \"192.168.1.5\"\n"
            + "\t\t}],\n"
            + "\t\t\"bindingHostId\": \"ephost_0\",\n"
            + "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"revisionNumber\": 0,\n"
            + "\t\t\"resourceRequest\": 0,\n"
            + "\t\t\"uplinkStatusPropagation\": false,\n"
            + "\t\t\"macLearningEnabled\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n"
            + "\t\t\"name\": \"test_cni_port5\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"vpcs\": [{\n"
            + "\t\t\"cidr\": \"192.168.0.0/16\",\n"
            + "\t\t\"adminStateUp\": false,\n"
            + "\t\t\"portSecurityEnabled\": false,\n"
            + "\t\t\"routerExternal\": false,\n"
            + "\t\t\"shared\": false,\n"
            + "\t\t\"vlanTransparent\": false,\n"
            + "\t\t\"isDefault\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\"name\": \"test_vpc\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"subnets\": [{\n"
            + "\t\t\"tunnelId\": 88888,\n"
            + "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\"cidr\": \"192.168.1.0/24\",\n"
            + "\t\t\"availabilityZone\": \"uswest-1\",\n"
            + "\t\t\"gatewayIp\": \"192.168.1.1\",\n"
            + "\t\t\"dhcpEnable\": false,\n"
            + "\t\t\"dnsPublishFixedIp\": false,\n"
            + "\t\t\"useDefaultSubnetpool\": false,\n"
            + "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\"name\": \"test_subnet1\",\n"
            + "\t\t\"description\": \"\"\n"
            + "\t}],\n"
            + "\t\"securityGroupEntities\": [{}, {}]\n"
            + "}";
    String result =
        "{\n"
            + "\t\"10.213.43.187\": {\n"
            + "\t\t\"bitField0_\": 0,\n"
            + "\t\t\"formatVersion_\": 0,\n"
            + "\t\t\"vpcStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 851612649\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1068653077\n"
            + "\t\t}],\n"
            + "\t\t\"subnetStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88888,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -572469531\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1003882173\n"
            + "\t\t}],\n"
            + "\t\t\"portStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port2\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:55\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040234\n"
            + "\t\t\t\t}, {\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.3\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040263\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -1801186197\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1344925595\n"
            + "\t\t}],\n"
            + "\t\t\"securityGroupStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 0,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"securityGroupRules_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t}],\n"
            + "\t\t\"dhcpStates_\": [],\n"
            + "\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\"unknownFields\": {\n"
            + "\t\t\t\"fields\": {},\n"
            + "\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t},\n"
            + "\t\t\"memoizedSize\": -1,\n"
            + "\t\t\"memoizedHashCode\": 0\n"
            + "\t}\n"
            + "}";
    handleData(input,result);
  }
  @Test
  public void transformBulkCreate2PortsOn4Host2Subnet2VPCs2FixedIp()
  {
    String input="{\n" +
            "\t\"rsType\": \"PORT\",\n" +
            "\t\"opType\": \"CREATE\",\n" +
            "\t\"allOrNone\": true,\n" +
            "\t\"portStates\": [{\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.4\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88888,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:55\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n" +
            "\t\t\"name\": \"test_cni_port2\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.4\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88889,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:56\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.4\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.5\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\",\n" +
            "\t\t\"name\": \"test_cni_port3\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.6\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88889,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:57\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\",\n" +
            "\t\t\"name\": \"test_cni_port4\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.6\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88888,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:58\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n" +
            "\t\t\"name\": \"test_cni_port5\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"vpcs\": [{\n" +
            "\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\"adminStateUp\": false,\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"routerExternal\": false,\n" +
            "\t\t\"shared\": false,\n" +
            "\t\t\"vlanTransparent\": false,\n" +
            "\t\t\"isDefault\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\"adminStateUp\": false,\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"routerExternal\": false,\n" +
            "\t\t\"shared\": false,\n" +
            "\t\t\"vlanTransparent\": false,\n" +
            "\t\t\"isDefault\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"subnets\": [{\n" +
            "\t\t\"tunnelId\": 88888,\n" +
            "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\"dhcpEnable\": false,\n" +
            "\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"tunnelId\": 88889,\n" +
            "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\"dhcpEnable\": false,\n" +
            "\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"securityGroupEntities\": [{}, {}]\n" +
            "}";
    String result =
        "{\n"
            + "\t\"1.2.3.6\": {\n"
            + "\t\t\"bitField0_\": 0,\n"
            + "\t\t\"formatVersion_\": 0,\n"
            + "\t\t\"vpcStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 415036460\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1292845484\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 851612649\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1068653077\n"
            + "\t\t}],\n"
            + "\t\t\"subnetStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88889,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -440708238\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 529992374\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88888,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -572469531\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1003882173\n"
            + "\t\t}],\n"
            + "\t\t\"portStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port4\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:57\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.2.2\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877124972\n"
            + "\t\t\t\t}, {\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.2.3\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877125001\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 260914510\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1016303954\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port5\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:58\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040234\n"
            + "\t\t\t\t}, {\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.3\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040263\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 1855863341\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 25179797\n"
            + "\t\t}],\n"
            + "\t\t\"securityGroupStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 0,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"securityGroupRules_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t}],\n"
            + "\t\t\"dhcpStates_\": [],\n"
            + "\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\"unknownFields\": {\n"
            + "\t\t\t\"fields\": {},\n"
            + "\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t},\n"
            + "\t\t\"memoizedSize\": -1,\n"
            + "\t\t\"memoizedHashCode\": 0\n"
            + "\t},\n"
            + "\t\"1.2.3.4\": {\n"
            + "\t\t\"bitField0_\": 0,\n"
            + "\t\t\"formatVersion_\": 0,\n"
            + "\t\t\"vpcStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 415036460\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1292845484\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n"
            + "\t\t\t\t\"tunnelId_\": 0,\n"
            + "\t\t\t\t\"subnetIds_\": [],\n"
            + "\t\t\t\t\"routes_\": [],\n"
            + "\t\t\t\t\"transitRouters_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 851612649\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1068653077\n"
            + "\t\t}],\n"
            + "\t\t\"subnetStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88889,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -440708238\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 529992374\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"cidr_\": \"\",\n"
            + "\t\t\t\t\"tunnelId_\": 88888,\n"
            + "\t\t\t\t\"dhcpEnable_\": false,\n"
            + "\t\t\t\t\"availabilityZone_\": \"\",\n"
            + "\t\t\t\t\"primaryDns_\": \"\",\n"
            + "\t\t\t\t\"secondaryDns_\": \"\",\n"
            + "\t\t\t\t\"transitSwitches_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -572469531\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1003882173\n"
            + "\t\t}],\n"
            + "\t\t\"portStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port3\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:56\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.2.4\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877125030\n"
            + "\t\t\t\t}, {\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.2.5\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877125059\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -1356553610\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1354368730\n"
            + "\t\t}, {\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 1,\n"
            + "\t\t\t\t\"revisionNumber_\": 1,\n"
            + "\t\t\t\t\"messageType_\": 1,\n"
            + "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n"
            + "\t\t\t\t\"networkType_\": 0,\n"
            + "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"test_cni_port2\",\n"
            + "\t\t\t\t\"networkNs_\": \"\",\n"
            + "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:55\",\n"
            + "\t\t\t\t\"adminStateUp_\": true,\n"
            + "\t\t\t\t\"fixedIps_\": [{\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040234\n"
            + "\t\t\t\t}, {\n"
            + "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n"
            + "\t\t\t\t\t\"ipAddress_\": \"192.168.1.3\",\n"
            + "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\t\"memoizedHashCode\": 877040263\n"
            + "\t\t\t\t}],\n"
            + "\t\t\t\t\"allowAddressPairs_\": [],\n"
            + "\t\t\t\t\"securityGroupIds_\": [],\n"
            + "\t\t\t\t\"vethName_\": \"\",\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": -1801186197\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 1344925595\n"
            + "\t\t}],\n"
            + "\t\t\"securityGroupStates_\": [{\n"
            + "\t\t\t\"operationType_\": 0,\n"
            + "\t\t\t\"configuration_\": {\n"
            + "\t\t\t\t\"bitField0_\": 0,\n"
            + "\t\t\t\t\"formatVersion_\": 0,\n"
            + "\t\t\t\t\"revisionNumber_\": 0,\n"
            + "\t\t\t\t\"id_\": \"\",\n"
            + "\t\t\t\t\"projectId_\": \"\",\n"
            + "\t\t\t\t\"vpcId_\": \"\",\n"
            + "\t\t\t\t\"name_\": \"\",\n"
            + "\t\t\t\t\"securityGroupRules_\": [],\n"
            + "\t\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\t\"unknownFields\": {\n"
            + "\t\t\t\t\"fields\": {},\n"
            + "\t\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t\t},\n"
            + "\t\t\t\"memoizedSize\": -1,\n"
            + "\t\t\t\"memoizedHashCode\": 0\n"
            + "\t\t}],\n"
            + "\t\t\"dhcpStates_\": [],\n"
            + "\t\t\"memoizedIsInitialized\": 1,\n"
            + "\t\t\"unknownFields\": {\n"
            + "\t\t\t\"fields\": {},\n"
            + "\t\t\t\"fieldsDescending\": {}\n"
            + "\t\t},\n"
            + "\t\t\"memoizedSize\": -1,\n"
            + "\t\t\"memoizedHashCode\": 0\n"
            + "\t}\n"
            + "}";
    handleData(input,result);
  }
  private void handleData(String input, String result) {
    Gson g = new Gson();
    NetworkConfiguration networkConfiguration = g.fromJson(input, NetworkConfiguration.class);
    final Map<String, Goalstate.GoalState> programOutput =
        goalStateUtil.transformNorthToSouth(networkConfiguration);

    final Map<String, Goalstate.GoalState> expectedOutput =
        (Map<String, Goalstate.GoalState>)
            g.fromJson(result, new TypeToken<Map<String, Goalstate.GoalState>>() {}.getType());
    programOutput.entrySet().stream()
        .forEach(
            e -> {
              Assert.assertTrue(
                  compareInternalEntity(e.getValue(), expectedOutput.get(e.getKey())));
            });
  }

  private boolean compareInternalEntity(Goalstate.GoalState o1, Goalstate.GoalState o2) {
    List<Port.PortState> portStatesList = new ArrayList(o1.getPortStatesList());
    List<Subnet.SubnetState> subnetStatesList = new ArrayList((o1.getSubnetStatesList()));
    List<Vpc.VpcState> vpcStatesList = new ArrayList(o1.getVpcStatesList());
    List<SecurityGroup.SecurityGroupState> securityGroupStatesList =
        o1.getSecurityGroupStatesList();

    List<Port.PortState> portStatesList1 = new ArrayList(o2.getPortStatesList());
    List<Subnet.SubnetState> subnetStatesList1 = new ArrayList(o2.getSubnetStatesList());
    List<Vpc.VpcState> vpcStatesList1 = new ArrayList(o2.getVpcStatesList());
    List<SecurityGroup.SecurityGroupState> securityGroupStatesList1 =
        o2.getSecurityGroupStatesList();

    portStatesList.removeAll(portStatesList1);
    subnetStatesList.removeAll(subnetStatesList1);
    vpcStatesList1.removeAll(vpcStatesList);
    boolean b = portStatesList.isEmpty() && subnetStatesList.isEmpty() && vpcStatesList1.isEmpty();
    LOG.debug(String.valueOf(b));
    return b;
  }

  @Test
  public void transformNeighbor2PortsOn4Host2Subnet2VPCs2FixedIp()
  {
    String input="{\n" +
            "\t\"rsType\": \"PORT\",\n" +
            "\t\"opType\": \"CREATE\",\n" +
            "\t\"allOrNone\": true,\n" +
            "\t\"portStates\": [{\n" +
            "\t\t\"neighborIps\": [{\n" +
            "\t\t\t\"hostIp\": \"1.2.3.6\",\n" +
            "\t\t\t\"hostId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"portId\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.4\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88888,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:55\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n" +
            "\t\t\"name\": \"test_cni_port2\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"neighborIps\": [{\n" +
            "\t\t\t\"hostIp\": \"1.2.3.6\",\n" +
            "\t\t\t\"hostId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"portId\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.4\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88889,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:56\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.4\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.5\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\",\n" +
            "\t\t\"name\": \"test_cni_port3\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"neighborIps\": [{\n" +
            "\t\t\t\"hostIp\": \"1.2.3.4\",\n" +
            "\t\t\t\"hostId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"portId\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.6\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88889,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:57\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.2.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\",\n" +
            "\t\t\"name\": \"test_cni_port4\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"neighborIps\": [{\n" +
            "\t\t\t\"hostIp\": \"1.2.3.4\",\n" +
            "\t\t\t\"hostId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\"portId\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostIP\": \"1.2.3.6\",\n" +
            "\t\t\"subnetEntities\": [{\n" +
            "\t\t\t\"tunnelId\": 88888,\n" +
            "\t\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\t\"dhcpEnable\": false,\n" +
            "\t\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"vpcEntities\": [{\n" +
            "\t\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\t\"adminStateUp\": false,\n" +
            "\t\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\t\"routerExternal\": false,\n" +
            "\t\t\t\"shared\": false,\n" +
            "\t\t\t\"vlanTransparent\": false,\n" +
            "\t\t\t\"isDefault\": false,\n" +
            "\t\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\t\"description\": \"\"\n" +
            "\t\t}],\n" +
            "\t\t\"adminStateUp\": true,\n" +
            "\t\t\"macAddress\": \"86:ea:77:ad:52:58\",\n" +
            "\t\t\"vethName\": \"veth0\",\n" +
            "\t\t\"fastPath\": true,\n" +
            "\t\t\"fixedIps\": [{\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.2\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"subnetId\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\"ipAddress\": \"192.168.1.3\"\n" +
            "\t\t}],\n" +
            "\t\t\"bindingHostId\": \"ephost_0\",\n" +
            "\t\t\"networkNamespace\": \"/var/run/netns/test_netw_ns\",\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"revisionNumber\": 0,\n" +
            "\t\t\"resourceRequest\": 0,\n" +
            "\t\t\"uplinkStatusPropagation\": false,\n" +
            "\t\t\"macLearningEnabled\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n" +
            "\t\t\"name\": \"test_cni_port5\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"vpcs\": [{\n" +
            "\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\"adminStateUp\": false,\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"routerExternal\": false,\n" +
            "\t\t\"shared\": false,\n" +
            "\t\t\"vlanTransparent\": false,\n" +
            "\t\t\"isDefault\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"cidr\": \"192.168.0.0/16\",\n" +
            "\t\t\"adminStateUp\": false,\n" +
            "\t\t\"portSecurityEnabled\": false,\n" +
            "\t\t\"routerExternal\": false,\n" +
            "\t\t\"shared\": false,\n" +
            "\t\t\"vlanTransparent\": false,\n" +
            "\t\t\"isDefault\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\"name\": \"test_vpc\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"subnets\": [{\n" +
            "\t\t\"tunnelId\": 88888,\n" +
            "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\"cidr\": \"192.168.1.0/24\",\n" +
            "\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\"gatewayIp\": \"192.168.1.1\",\n" +
            "\t\t\"dhcpEnable\": false,\n" +
            "\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\"name\": \"test_subnet1\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}, {\n" +
            "\t\t\"tunnelId\": 88889,\n" +
            "\t\t\"vpcId\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\"cidr\": \"192.168.2.0/24\",\n" +
            "\t\t\"availabilityZone\": \"uswest-1\",\n" +
            "\t\t\"gatewayIp\": \"192.168.2.1\",\n" +
            "\t\t\"dhcpEnable\": false,\n" +
            "\t\t\"dnsPublishFixedIp\": false,\n" +
            "\t\t\"useDefaultSubnetpool\": false,\n" +
            "\t\t\"projectId\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\"id\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\"name\": \"test_subnet2\",\n" +
            "\t\t\"description\": \"\"\n" +
            "\t}],\n" +
            "\t\"securityGroupEntities\": [{}, {}]\n" +
            "}";
    String result="{\n" +
            "\t\"1.2.3.6\": {\n" +
            "\t\t\"bitField0_\": 0,\n" +
            "\t\t\"formatVersion_\": 0,\n" +
            "\t\t\"vpcStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n" +
            "\t\t\t\t\"tunnelId_\": 0,\n" +
            "\t\t\t\t\"subnetIds_\": [],\n" +
            "\t\t\t\t\"routes_\": [],\n" +
            "\t\t\t\t\"transitRouters_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 415036460\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1292845484\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n" +
            "\t\t\t\t\"tunnelId_\": 0,\n" +
            "\t\t\t\t\"subnetIds_\": [],\n" +
            "\t\t\t\t\"routes_\": [],\n" +
            "\t\t\t\t\"transitRouters_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 851612649\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1068653077\n" +
            "\t\t}],\n" +
            "\t\t\"subnetStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"\",\n" +
            "\t\t\t\t\"tunnelId_\": 88889,\n" +
            "\t\t\t\t\"dhcpEnable_\": false,\n" +
            "\t\t\t\t\"availabilityZone_\": \"\",\n" +
            "\t\t\t\t\"primaryDns_\": \"\",\n" +
            "\t\t\t\t\"secondaryDns_\": \"\",\n" +
            "\t\t\t\t\"transitSwitches_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -440708238\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 529992374\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"\",\n" +
            "\t\t\t\t\"tunnelId_\": 88888,\n" +
            "\t\t\t\t\"dhcpEnable_\": false,\n" +
            "\t\t\t\t\"availabilityZone_\": \"\",\n" +
            "\t\t\t\t\"primaryDns_\": \"\",\n" +
            "\t\t\t\t\"secondaryDns_\": \"\",\n" +
            "\t\t\t\t\"transitSwitches_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -572469531\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1003882173\n" +
            "\t\t}],\n" +
            "\t\t\"portStates_\": [{\n" +
            "\t\t\t\"operationType_\": 5,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"messageType_\": 0,\n" +
            "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\",\n" +
            "\t\t\t\t\"networkType_\": 5,\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"test_cni_port4\",\n" +
            "\t\t\t\t\"networkNs_\": \"\",\n" +
            "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:57\",\n" +
            "\t\t\t\t\"adminStateUp_\": true,\n" +
            "\t\t\t\t\"hostInfo_\": {\n" +
            "\t\t\t\t\t\"ipAddress_\": \"1.2.3.4\",\n" +
            "\t\t\t\t\t\"macAddress_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d0\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": -1584629934\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"fixedIps_\": [{\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.2.2\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877124972\n" +
            "\t\t\t\t}, {\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.2.3\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877125001\n" +
            "\t\t\t\t}],\n" +
            "\t\t\t\t\"allowAddressPairs_\": [],\n" +
            "\t\t\t\t\"securityGroupIds_\": [],\n" +
            "\t\t\t\t\"vethName_\": \"\",\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -135761614\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": -1897084705\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 5,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"messageType_\": 0,\n" +
            "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n" +
            "\t\t\t\t\"networkType_\": 5,\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"test_cni_port5\",\n" +
            "\t\t\t\t\"networkNs_\": \"\",\n" +
            "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:58\",\n" +
            "\t\t\t\t\"adminStateUp_\": true,\n" +
            "\t\t\t\t\"hostInfo_\": {\n" +
            "\t\t\t\t\t\"ipAddress_\": \"1.2.3.4\",\n" +
            "\t\t\t\t\t\"macAddress_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d3\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": -1584629847\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"fixedIps_\": [{\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877040234\n" +
            "\t\t\t\t}, {\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.1.3\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877040263\n" +
            "\t\t\t\t}],\n" +
            "\t\t\t\t\"allowAddressPairs_\": [],\n" +
            "\t\t\t\t\"securityGroupIds_\": [],\n" +
            "\t\t\t\t\"vethName_\": \"\",\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 376094926\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 61853067\n" +
            "\t\t}],\n" +
            "\t\t\"securityGroupStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 0,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"securityGroupRules_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 0\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 0\n" +
            "\t\t}],\n" +
            "\t\t\"dhcpStates_\": [],\n" +
            "\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\"unknownFields\": {\n" +
            "\t\t\t\"fields\": {},\n" +
            "\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t},\n" +
            "\t\t\"memoizedSize\": -1,\n" +
            "\t\t\"memoizedHashCode\": 0\n" +
            "\t},\n" +
            "\t\"1.2.3.4\": {\n" +
            "\t\t\"bitField0_\": 0,\n" +
            "\t\t\"formatVersion_\": 0,\n" +
            "\t\t\"vpcStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n" +
            "\t\t\t\t\"tunnelId_\": 0,\n" +
            "\t\t\t\t\"subnetIds_\": [],\n" +
            "\t\t\t\t\"routes_\": [],\n" +
            "\t\t\t\t\"transitRouters_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 415036460\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1292845484\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"id_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"192.168.0.0/16\",\n" +
            "\t\t\t\t\"tunnelId_\": 0,\n" +
            "\t\t\t\t\"subnetIds_\": [],\n" +
            "\t\t\t\t\"routes_\": [],\n" +
            "\t\t\t\t\"transitRouters_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 851612649\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1068653077\n" +
            "\t\t}],\n" +
            "\t\t\"subnetStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"\",\n" +
            "\t\t\t\t\"tunnelId_\": 88889,\n" +
            "\t\t\t\t\"dhcpEnable_\": false,\n" +
            "\t\t\t\t\"availabilityZone_\": \"\",\n" +
            "\t\t\t\t\"primaryDns_\": \"\",\n" +
            "\t\t\t\t\"secondaryDns_\": \"\",\n" +
            "\t\t\t\t\"transitSwitches_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -440708238\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 529992374\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"9192a4d4-ffff-4ece-b3f0-8d36e3d88039\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"cidr_\": \"\",\n" +
            "\t\t\t\t\"tunnelId_\": 88888,\n" +
            "\t\t\t\t\"dhcpEnable_\": false,\n" +
            "\t\t\t\t\"availabilityZone_\": \"\",\n" +
            "\t\t\t\t\"primaryDns_\": \"\",\n" +
            "\t\t\t\t\"secondaryDns_\": \"\",\n" +
            "\t\t\t\t\"transitSwitches_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -572469531\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1003882173\n" +
            "\t\t}],\n" +
            "\t\t\"portStates_\": [{\n" +
            "\t\t\t\"operationType_\": 5,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"messageType_\": 0,\n" +
            "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n" +
            "\t\t\t\t\"networkType_\": 5,\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"test_cni_port2\",\n" +
            "\t\t\t\t\"networkNs_\": \"\",\n" +
            "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:55\",\n" +
            "\t\t\t\t\"adminStateUp_\": true,\n" +
            "\t\t\t\t\"hostInfo_\": {\n" +
            "\t\t\t\t\t\"ipAddress_\": \"1.2.3.6\",\n" +
            "\t\t\t\t\t\"macAddress_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d7\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": -1584515993\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"fixedIps_\": [{\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.1.2\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877040234\n" +
            "\t\t\t\t}, {\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594e\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.1.3\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877040263\n" +
            "\t\t\t\t}],\n" +
            "\t\t\t\t\"allowAddressPairs_\": [],\n" +
            "\t\t\t\t\"securityGroupIds_\": [],\n" +
            "\t\t\t\t\"vethName_\": \"\",\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 378000906\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 117126487\n" +
            "\t\t}, {\n" +
            "\t\t\t\"operationType_\": 5,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 1,\n" +
            "\t\t\t\t\"revisionNumber_\": 1,\n" +
            "\t\t\t\t\"messageType_\": 0,\n" +
            "\t\t\t\t\"id_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\",\n" +
            "\t\t\t\t\"networkType_\": 5,\n" +
            "\t\t\t\t\"projectId_\": \"3dda2801-d675-4688-a63f-dcda8d327f50\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"test_cni_port3\",\n" +
            "\t\t\t\t\"networkNs_\": \"\",\n" +
            "\t\t\t\t\"macAddress_\": \"86:ea:77:ad:52:56\",\n" +
            "\t\t\t\t\"adminStateUp_\": true,\n" +
            "\t\t\t\t\"hostInfo_\": {\n" +
            "\t\t\t\t\t\"ipAddress_\": \"1.2.3.6\",\n" +
            "\t\t\t\t\t\"macAddress_\": \"f37810eb-7f83-45fa-a4d4-1b31e75399d6\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": -1584516022\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"fixedIps_\": [{\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.2.4\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877125030\n" +
            "\t\t\t\t}, {\n" +
            "\t\t\t\t\t\"subnetId_\": \"a87e0f87-a2d9-44ef-9194-9a62f178594f\",\n" +
            "\t\t\t\t\t\"ipAddress_\": \"192.168.2.5\",\n" +
            "\t\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\t\"memoizedHashCode\": 877125059\n" +
            "\t\t\t\t}],\n" +
            "\t\t\t\t\"allowAddressPairs_\": [],\n" +
            "\t\t\t\t\"securityGroupIds_\": [],\n" +
            "\t\t\t\t\"vethName_\": \"\",\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": -924124082\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 1010207499\n" +
            "\t\t}],\n" +
            "\t\t\"securityGroupStates_\": [{\n" +
            "\t\t\t\"operationType_\": 0,\n" +
            "\t\t\t\"configuration_\": {\n" +
            "\t\t\t\t\"bitField0_\": 0,\n" +
            "\t\t\t\t\"formatVersion_\": 0,\n" +
            "\t\t\t\t\"revisionNumber_\": 0,\n" +
            "\t\t\t\t\"id_\": \"\",\n" +
            "\t\t\t\t\"projectId_\": \"\",\n" +
            "\t\t\t\t\"vpcId_\": \"\",\n" +
            "\t\t\t\t\"name_\": \"\",\n" +
            "\t\t\t\t\"securityGroupRules_\": [],\n" +
            "\t\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\t\"memoizedHashCode\": 0\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\t\"unknownFields\": {\n" +
            "\t\t\t\t\"fields\": {},\n" +
            "\t\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t\t},\n" +
            "\t\t\t\"memoizedSize\": -1,\n" +
            "\t\t\t\"memoizedHashCode\": 0\n" +
            "\t\t}],\n" +
            "\t\t\"dhcpStates_\": [],\n" +
            "\t\t\"memoizedIsInitialized\": 1,\n" +
            "\t\t\"unknownFields\": {\n" +
            "\t\t\t\"fields\": {},\n" +
            "\t\t\t\"fieldsDescending\": {}\n" +
            "\t\t},\n" +
            "\t\t\"memoizedSize\": -1,\n" +
            "\t\t\"memoizedHashCode\": 0\n" +
            "\t}\n" +
            "}";
    handleData(input,result);
  }
  }
