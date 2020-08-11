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
package com.futurewei.alcor.securitygroup.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.securitygroup.exception.SecurityGroupNotFound;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class PortBindingSecurityGroupRepository {
    private static final Logger LOG = LoggerFactory.getLogger(PortBindingSecurityGroupRepository.class);

    private ICache<String, PortBindingSecurityGroup> bindingCache;
    private ICache<String, SecurityGroup> securityGroupCache;

    @Autowired
    public PortBindingSecurityGroupRepository(CacheFactory cacheFactory) {
        bindingCache = cacheFactory.getCache(PortBindingSecurityGroup.class);
        securityGroupCache = cacheFactory.getCache(SecurityGroup.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("PortBindingSecurityGroupRepository init done");
    }

    @DurationStatistics
    public Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupBySecurityGroupId(String securityGroupId) throws CacheException {
        Map<String, String[]> filterParams = new HashMap<>();
        filterParams.put("security_group_id", new String[] {securityGroupId});

        Map<String, PortBindingSecurityGroup> portBindingSecurityGroupMap = bindingCache.getAll();
        return portBindingSecurityGroupMap.values();
    }

    @DurationStatistics
    public synchronized void addPortBindingSecurityGroup(List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception {
        Map<String, PortBindingSecurityGroup> portBindingSecurityGroupMap = portBindingSecurityGroups
                .stream()
                .collect(Collectors.toMap(PortBindingSecurityGroup::getId, Function.identity()));

        bindingCache.putAll(portBindingSecurityGroupMap);
    }

    @DurationStatistics
    public synchronized void deleteSecurityGroupBinding(List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception {
        for (PortBindingSecurityGroup portBindingSecurityGroup: portBindingSecurityGroups) {
            bindingCache.remove(portBindingSecurityGroup.getId());
        }
    }
}
