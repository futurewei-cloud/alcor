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
import com.futurewei.alcor.securitygroup.exception.SecurityGroupRequired;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.HashSet;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SecurityGroupBindingsRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupBindingsRepository.class);

    private ICache<String, SecurityGroupBindings> bindingCache;
    private ICache<String, SecurityGroup> securityGroupCache;

    @Autowired
    public SecurityGroupBindingsRepository(CacheFactory cacheFactory) {
        bindingCache = cacheFactory.getCache(SecurityGroupBindings.class);
        securityGroupCache = cacheFactory.getCache(SecurityGroup.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("SecurityGroupBindingsRepository init done");
    }

    public SecurityGroupBindings getSecurityGroupBindings(String id) throws CacheException {
        return bindingCache.get(id);
    }


    public void addSecurityGroupBinding(PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        // FIXME: https://github.com/futurewei-cloud/alcor/issues/264
        try (Transaction tx = bindingCache.getTransaction().start()) {
            String portId = portSecurityGroupsJson.getPortId();

            for (String securityGroupId: portSecurityGroupsJson.getSecurityGroups()) {
                SecurityGroup securityGroup = securityGroupCache.get(securityGroupId);
                if (securityGroup == null) {
                    throw new SecurityGroupRequired();
                }

                SecurityGroupBindings securityGroupBindings = bindingCache.get(securityGroupId);
                if (securityGroupBindings == null) {
                    securityGroupBindings = new SecurityGroupBindings();
                    securityGroupBindings.setSecurityGroupId(securityGroupId);
                    securityGroupBindings.setBindings(new HashSet<>());
                }

                if (!securityGroupBindings.getBindings().contains(portId)) {
                    securityGroupBindings.getBindings().add(portId);
                }

                bindingCache.put(securityGroupBindings.getSecurityGroupId(), securityGroupBindings);
            }

            tx.commit();
        }
    }

    public void deleteSecurityGroupBinding(PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        try (Transaction tx = bindingCache.getTransaction().start()) {
            String portId = portSecurityGroupsJson.getPortId();

            for (String securityGroupId: portSecurityGroupsJson.getSecurityGroups()) {
                SecurityGroupBindings securityGroupBindings = bindingCache.get(securityGroupId);
                if (securityGroupBindings != null) {
                    securityGroupBindings.getBindings().remove(portId);
                    if (securityGroupBindings.getBindings().size() > 0) {
                        bindingCache.put(securityGroupBindings.getSecurityGroupId(), securityGroupBindings);
                    } else {
                        bindingCache.remove(securityGroupBindings.getSecurityGroupId());
                    }
                }
            }

            tx.commit();
        }
    }
}
