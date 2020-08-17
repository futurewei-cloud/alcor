/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.cache;

import com.futurewei.alcor.common.cache.entity.TestEntity;
import com.futurewei.alcor.common.exception.QueryParamTypeNotSupportException;
import com.futurewei.alcor.common.utils.ControllerUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ControllerUtilTest {

    @Test
    public void transformUrlPathParamsTest() throws QueryParamTypeNotSupportException {
        Map<String, String[]> params = new HashMap<>();
        params.put("project_id", new String[]{"asdfasf", "asdfasdf"});
        params.put("id", new String[]{"1"});
        params.put("name", new String[]{"dsefad"});
        params.put("description", new String[]{"desc1"});
        params.put("shared", new String[]{"false"});
        params.put("count", new String[]{"1", "2"});
        params.put("revision_number", new String[]{"3", "4"});
        params.put("egress_firewall_policy_id", new String[]{"abcdef", "abcdfe", "bacdef", "abdcef"});
        params.put("enable_dhcp", new String[]{"true"});
        params.put("ip_version", new String[]{"4", "6"});

        Map<String, Object[]> tranformedMap = ControllerUtil.transformUrlPathParams(params, TestEntity.class);
        System.out.println(tranformedMap.toString());

        Assert.assertEquals("asdfasf", tranformedMap.get("projectId")[0]);
        Assert.assertEquals(false, tranformedMap.get("shared")[0]);
        Assert.assertEquals(1, tranformedMap.get("count")[0]);
        Assert.assertEquals(4, tranformedMap.get("revisionNumber")[1]);
        Assert.assertEquals("bacdef", tranformedMap.get("egressFirewallPolicyId")[2]);
        Assert.assertEquals(Boolean.valueOf("true"), tranformedMap.get("enableDhcp")[0]);
        Assert.assertEquals(Integer.valueOf(6), tranformedMap.get("ipVersion")[1]);
        Assert.assertEquals("1", tranformedMap.get("id")[0]);
        Assert.assertEquals("dsefad", tranformedMap.get("name")[0]);
        Assert.assertEquals("desc1", tranformedMap.get("description")[0]);

    }

    @Test
    public void testEntity(){
        TestEntity testEntity = new TestEntity();
        testEntity.setCount(1);
        testEntity.setEgressFirewallPolicyId("abcdef");
        testEntity.setShared(true);
        testEntity.setProjectId("sdfasdffas");
        testEntity.setRevisionNumber(6);
        testEntity.setIpVersion(6);

        Assert.assertTrue(String.valueOf(testEntity.getCount()).equals("1"));
        Assert.assertTrue(String.valueOf(testEntity.getEgressFirewallPolicyId()).equals("abcdef"));
        Assert.assertTrue(String.valueOf(testEntity.isShared()).equals("true"));
        Assert.assertTrue(String.valueOf(testEntity.getProjectId()).equals("sdfasdffas"));
        Assert.assertTrue(String.valueOf(testEntity.getRevisionNumber()).equals("6"));
        Assert.assertTrue(String.valueOf(testEntity.getIpVersion()).equals("6"));

    }

    @Test
    public void test(){
        Annotation[] as = TestEntity.class.getDeclaredAnnotations();
        for(Annotation a: as){
            System.out.println(a.annotationType().getSimpleName());
        }
    }

}
