package com.futurewei.alcor.dataplane.client;

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import org.springframework.stereotype.Component;

@Component
public interface ClientManager {
    boolean isFastPath(InternalPortEntity portEntity) throws Exception;
}
