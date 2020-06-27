package com.futurewei.alcor.web.entity.dataplane;

import lombok.Data;

import java.util.List;

@Data
public class InternalDPMResultList {
    private List<InternalDPMResult> resultList;
    private long overrallTime;
}
