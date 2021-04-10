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
        Map<String, Object[]> filterParams = new HashMap<>();
        filterParams.put("securityGroupId", new String[] {securityGroupId});

        Map<String, PortBindingSecurityGroup> portBindingSecurityGroupMap = bindingCache.getAll(filterParams);
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
