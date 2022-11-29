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
package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class SecurityGroupRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupRepository.class);

    private ICache<String, SecurityGroupRule> securityGroupRuleCache;

    @Autowired
    public SecurityGroupRepository(CacheFactory cacheFactory) {
        securityGroupRuleCache = cacheFactory.getCache(SecurityGroupRule.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("SecurityGroupRepository init done");
    }

    @DurationStatistics
    public synchronized void addSecurityGroupRules(List<SecurityGroupRule> securityGroupRules) throws Exception {
        var securityGroupRuleMap = securityGroupRules.stream().collect(Collectors.toMap(SecurityGroupRule::getId, Function.identity()));
        securityGroupRuleCache.putAll(securityGroupRuleMap);
    }

    @DurationStatistics
    public synchronized void deleteSecurityGroupRules(List<String> securityGroupIds) throws Exception {
        securityGroupIds.forEach(securityGroupId -> {
            try {
                securityGroupRuleCache.remove(securityGroupId);
            } catch (CacheException e) {
                e.printStackTrace();
            }
        });
    }

    @DurationStatistics
    public SecurityGroupRule getSecurityGroupRule(String securityGroupRuleId) throws CacheException {
        return securityGroupRuleCache.get(securityGroupRuleId);
    }

    @DurationStatistics
    public List<SecurityGroupRule> getSecurityGroupRules(List<String> securityGroupRuleIds) throws CacheException {
        List<SecurityGroupRule> securityGroupRules = new ArrayList<>();
        for (String securityGroupId : securityGroupRuleIds) {
            SecurityGroupRule securityGroupRule = securityGroupRuleCache.get(securityGroupId);
            securityGroupRules.add(securityGroupRule);
        }
        return securityGroupRules;
    }

    @DurationStatistics
    public Collection<SecurityGroupRule> getSecurityGroupRules(String scurityGroupId) throws CacheException {
        Map<String, Object[]> queryParams =  new HashMap<>();
        Object[] value = new Object[1];
        value[0] = scurityGroupId;
        queryParams.put("securityGroupId", value);
        return securityGroupRuleCache.getAll(queryParams).values();
    }
}
