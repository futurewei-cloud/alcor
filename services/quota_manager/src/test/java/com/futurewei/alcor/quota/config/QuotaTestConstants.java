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

package com.futurewei.alcor.quota.config;

import com.futurewei.alcor.quota.utils.QuotaUtils;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;

import java.util.HashMap;
import java.util.Map;

public class QuotaTestConstants {

    public static final String PROJECT_ID = "3d53801c-32ce-4e97-9572-bb966f4de79c";
    public static final String PROJECT_ID2 = "3d53801c-32ce-4e97-9572-bb966f4de79d";
    public static final String PROJECT_ID3 = "3d53801c-32ce-4e97-9572-bb966f4de79e";
    public static final String EMPTY_PROJECT_ID = "4d53801c-32ce-4e97-9572-bb966f4de79c";
    public static final String APPLY_ID = "5d53801c-32ce-4e97-9572-bb966f4de79c";
    public static final String EMPTY_APPLY_ID = "5d53801c-32ce-4e97-9572-bb966f4de79d";

    public static final String RESOURCE_URL_TEMP = "/project/%s/quotas/%s";
    public static final String COLLECT_URL_TEMP = "/project/%s/quotas";
    public static final String GET_DEFAULT_URL_TEMP = "/project/%s/quotas/%s/default";
    public static final String GET_DETAIL_URL_TEMP = "/project/%s/quotas/%s/details.json";
    public static final String QUOTA_APPLY_TEMP = "/project/%s/quota/apply";
    public static final String QUOTA_CANCEL_APPLY_TEMP = "/quota/apply/%s";

    public static final String FLOATING_IP = "floating_ip";
    public static final String NETWORK = "network";
    public static final String PORT = "port";
    public static final String RBAC_POLICY = "rbac_policy";
    public static final String ROUTER = "router";
    public static final String SECURITY_GROUP = "security_group";
    public static final String SECURITY_GROUP_RULE = "security_group_rule";
    public static final String SUBNET = "subnet";
    public static final String SUBNETPOOL = "subnetpool";

    public static final int FLOATING_IP_LIMIT = 51;
    public static final int NETWORK_LIMIT = 11;
    public static final int PORT_LIMIT = 51;
    public static final int RBAC_POLICY_LIMIT = -1;
    public static final int ROUTER_LIMIT = 11;
    public static final int SECURITY_GROUP_LIMIT = 11;
    public static final int SECURITY_GROUP_RULE_LIMIT = 101;
    public static final int SUBNET_LIMIT = 11;
    public static final int SUBNETPOOL_LIMIT = -1;

    public static final String updateQuota = "{\n" +
            "   \"quota\": {\n" +
            "      \"floatingip\": 52,\n" +
            "\t  \"network\": 17,\n" +
            "\t  \"port\": 52,\n" +
            "\t  \"rbac_policy\":  -1,\n" +
            "\t  \"router\": 10,\n" +
            "\t  \"security_group\": 10,\n" +
            "\t  \"security_group_rule\": 10,\n" +
            "\t  \"subnet\": 10,\n" +
            "\t  \"subnetpool\": -1\n" +
            "   }\n" +
            "}";

    public static final String applyQuota = "{\n" +
            " \"apply_id\": \"5d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"tenant_id\": \"3d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"project_id\": \"3d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"resource_deltas\": [\n" +
            "\t{\n" +
            "\t \"resource\": \"network\",\n" +
            "\t \"amount\": 2\n" +
            "\t},\n" +
            "\t{\n" +
            "\t \"resource\": \"subnet\",\n" +
            "\t \"amount\": 2\n" +
            "\t}]\n" +
            "}";

    public static final String applyEmptyDeltaQuota = "{\n" +
            " \"apply_id\": \"5d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"tenant_id\": \"3d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"project_id\": \"3d53801c-32ce-4e97-9572-bb966f4de79c\",\n" +
            " \"resource_deltas\": []" +
            "}";

    public static Map<String, QuotaEntity> buildProjectQuota(String projectId){
        Map<String, QuotaEntity> projectQuotaMap = new HashMap<>();
        buildProjectQuota(projectId, projectQuotaMap);
        return projectQuotaMap;
    }

    public static void buildProjectQuota(String projectId, Map<String, QuotaEntity> projectQuotaMap){
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, FLOATING_IP),
                new QuotaEntity(projectId, FLOATING_IP, FLOATING_IP_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, NETWORK),
                new QuotaEntity(projectId, NETWORK, NETWORK_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, PORT),
                new QuotaEntity(projectId, PORT, PORT_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, RBAC_POLICY),
                new QuotaEntity(projectId, RBAC_POLICY, RBAC_POLICY_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, ROUTER),
                new QuotaEntity(projectId, ROUTER, ROUTER_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, SECURITY_GROUP),
                new QuotaEntity(projectId, SECURITY_GROUP, SECURITY_GROUP_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, SECURITY_GROUP_RULE),
                new QuotaEntity(projectId, SECURITY_GROUP_RULE, SECURITY_GROUP_RULE_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, SUBNET),
                new QuotaEntity(projectId, SUBNET, SUBNET_LIMIT));
        projectQuotaMap.put(QuotaUtils.getCombineId(projectId, SUBNETPOOL),
                new QuotaEntity(projectId, SUBNETPOOL, SUBNETPOOL_LIMIT));
    }
}
