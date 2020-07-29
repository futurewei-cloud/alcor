package com.futurewei.alcor.web.entity.dataplane;

import lombok.Data;

import java.util.List;

@Data
public class InternalDPMResultList {
    private List<InternalDPMResult> resultList;
    private long overrallTime;
    private String resultMessage;

    public List<InternalDPMResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<InternalDPMResult> resultList) {
        this.resultList = resultList;
    }

    public long getOverrallTime() {
        return overrallTime;
    }

    public void setOverrallTime(long overrallTime) {
        this.overrallTime = overrallTime;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
