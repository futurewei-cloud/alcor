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
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class TopicInfo implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(NodeInfo.class);

    @JsonProperty("topic_name")
    private String topicName;

    @JsonProperty("subscribe_mapping")
//    Mapping of <Subscribed NodeId, key for Pulsar>
    private Map<String, String> subscribeMapping;

    public TopicInfo(String topicName) {
        this.topicName = topicName;
        this.subscribeMapping = new HashMap<>();
    }

    public TopicInfo(String topicName, HashMap<String, String> subscribeMapping) {
        this.topicName = topicName;
        this.subscribeMapping = subscribeMapping;
    }

    public TopicInfo(TopicInfo topicInfo, HashMap<String, String> subscribeMapping) {
        this.topicName = topicInfo.topicName;
        this.subscribeMapping = topicInfo.subscribeMapping;
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
