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
package com.futurewei.alcor.web.entity.topic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class VpcTopicInfo implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(NodeInfo.class);

    @JsonProperty("topic_name")
    private String topicName;

    @JsonProperty("subscribe_mapping")
//    Mapping of <Subscribed NodeIp, key for Pulsar>
    private Map<String, String> subscribeMapping;

    public VpcTopicInfo(String topicName) {
        this.topicName = topicName;
        this.subscribeMapping = new HashMap<>();
    }

    public VpcTopicInfo(String topicName, HashMap<String, String> subscribeMapping) {
        this.topicName = topicName;
        this.subscribeMapping = subscribeMapping;
    }

    public VpcTopicInfo(VpcTopicInfo vpcTopicInfo, HashMap<String, String> subscribeMapping) {
        this.topicName = vpcTopicInfo.topicName;
        this.subscribeMapping = vpcTopicInfo.subscribeMapping;
        this.subscribeMapping.putAll(subscribeMapping);
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Map<String, String> getSubscribeMapping() {
        return subscribeMapping;
    }

    public void setSubscribeMapping(HashMap<String, String> subscribeMapping) {
        this.subscribeMapping = subscribeMapping;
    }
}
