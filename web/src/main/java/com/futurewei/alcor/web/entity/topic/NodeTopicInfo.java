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

package com.futurewei.alcor.web.entity.topic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class NodeTopicInfo {
    private static final Logger logger = LoggerFactory.getLogger(NodeInfo.class);

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("unicast_topic")
    private String unicastTopic;

    @JsonProperty("multicast_topic")
    private String multicastTopic;

    @JsonProperty("group_topic")
    private String groupTopic;

    public NodeTopicInfo(String nodeId, String unicastTopic, String multicastTopic, String groupTopic) {
        this.nodeId = nodeId;
        this.unicastTopic = unicastTopic;
        this.multicastTopic = multicastTopic;
        this.groupTopic = groupTopic;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getUnicastTopic() {
        return unicastTopic;
    }

    public void setUnicastTopic(String unicastTopic) {
        this.unicastTopic = unicastTopic;
    }

    public String getMulticastTopic() {
        return multicastTopic;
    }

    public void setMulticastTopic(String multicastTopic) {
        this.multicastTopic = multicastTopic;
    }

    public String getGroupTopic() {
        return groupTopic;
    }

    public void setGroupTopic(String groupTopic) {
        this.groupTopic = groupTopic;
    }
}
