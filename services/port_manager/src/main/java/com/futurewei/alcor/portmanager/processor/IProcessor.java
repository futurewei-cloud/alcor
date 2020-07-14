/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;

public interface IProcessor {
    void createPort(PortEntity portEntity) throws Exception;
    void createPortBulk(List<PortEntity> portEntities) throws Exception;
    void updatePort(String portId, PortEntity portEntity) throws Exception;
    void waitProcessFinish();

    IProcessor getNextProcessor();
    void setNextProcessor(IProcessor nextProcessor);
    void setPortConfigCache(PortConfigCache portConfigCache);
    void setProjectId(String projectId);
    void setNetworkConfig(NetworkConfig networkConfig);
    void setPortRepository(PortRepository portRepository);
}
