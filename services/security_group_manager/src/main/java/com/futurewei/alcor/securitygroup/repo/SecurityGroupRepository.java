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
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class SecurityGroupRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupRepository.class);

    private ICache<String, SecurityGroup> securityGroupCache;
    private ICache<String, SecurityGroupRule> securityGroupRuleCache;

    @Autowired
    public SecurityGroupRepository(CacheFactory cacheFactory) {
        securityGroupCache = cacheFactory.getCache(SecurityGroup.class);
        securityGroupRuleCache = cacheFactory.getCache(SecurityGroupRule.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("SecurityGroupRepository init done");
    }

    @DurationStatistics
    public synchronized void addSecurityGroup(SecurityGroup securityGroup) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //Add all security group rules
            Map<String, SecurityGroupRule> securityGroupRules = securityGroup.getSecurityGroupRules()
                    .stream()
                    .collect(Collectors.toMap(SecurityGroupRule::getId, Function.identity()));
            securityGroupRuleCache.putAll(securityGroupRules);

            //Add security group
            securityGroupCache.put(securityGroup.getId(), securityGroup);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void addSecurityGroupBulk(List<SecurityGroup> securityGroups) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {
            //Add all security group rules
            Map<String, SecurityGroupRule> securityGroupRules = new HashMap<>();
            for (SecurityGroup securityGroup: securityGroups) {
                securityGroupRules.putAll(securityGroup.getSecurityGroupRules()
                        .stream()
                        .collect(Collectors.toMap(SecurityGroupRule::getId, Function.identity())));
            }
            securityGroupRuleCache.putAll(securityGroupRules);

            //Add all security groups
            Map<String, SecurityGroup> securityGroupMap = securityGroups
                    .stream()
                    .collect(Collectors.toMap(SecurityGroup::getId, Function.identity()));
            securityGroupCache.putAll(securityGroupMap);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deleteSecurityGroup(String id) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //If securityGroup is null, an exception will be thrown
            SecurityGroup securityGroup = securityGroupCache.get(id);

            //Delete all security group rules
            for (SecurityGroupRule rule: securityGroup.getSecurityGroupRules()) {
                securityGroupRuleCache.remove(rule.getId());
            }

            //Delete the security group
            securityGroupCache.remove(id);

            tx.commit();
        }
    }

    @DurationStatistics
    public SecurityGroup getSecurityGroup(String id) throws CacheException {
        return securityGroupCache.get(id);
    }

    @DurationStatistics
    public Map<String, SecurityGroup> getAllSecurityGroups() throws CacheException {
        return securityGroupCache.getAll();
    }

    @DurationStatistics
    public Map<String, SecurityGroup> getAllSecurityGroups(Map<String, Object[]> queryParams) throws CacheException {
        return securityGroupCache.getAll(queryParams);
    }

    /**
     * In order to find the default security group quickly bt tenant id,
     * when creating a default security group for each tenant, create an
     * additional entry with the key of tenant ID.
     * @param defaultSecurityGroup
     * @throws Exception
     */
    @DurationStatistics
    public synchronized void createDefaultSecurityGroup(SecurityGroup defaultSecurityGroup) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //Add all security group rules
            Map<String, SecurityGroupRule> defaultSecurityGroupRules = defaultSecurityGroup.getSecurityGroupRules()
                    .stream()
                    .collect(Collectors.toMap(SecurityGroupRule::getId, Function.identity()));
            securityGroupRuleCache.putAll(defaultSecurityGroupRules);

            //Add default security group
            securityGroupCache.put(defaultSecurityGroup.getId(), defaultSecurityGroup);
            securityGroupCache.put(defaultSecurityGroup.getTenantId(), defaultSecurityGroup);
            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deleteDefaultSecurityGroup(String id) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //If securityGroup is null, an exception will be thrown
            SecurityGroup securityGroup = securityGroupCache.get(id);

            //Delete all security group rules
            for (SecurityGroupRule rule: securityGroup.getSecurityGroupRules()) {
                securityGroupRuleCache.remove(rule.getId());
            }

            //Delete the default security group
            securityGroupCache.remove(id);
            securityGroupCache.remove(securityGroup.getTenantId());

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void addSecurityGroupRule(SecurityGroup securityGroup, SecurityGroupRule securityGroupRule) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //Add security group rule to security group
            securityGroup.getSecurityGroupRules().add(securityGroupRule);
            securityGroupCache.put(securityGroup.getId(), securityGroup);

            //Add security group rule
            securityGroupRuleCache.put(securityGroupRule.getId(), securityGroupRule);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void addSecurityGroupRuleBulk(List<SecurityGroupRule> securityGroupRules) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {
            //Add security group rule to security group
            for (SecurityGroupRule securityGroupRule: securityGroupRules) {
                SecurityGroup securityGroup = securityGroupCache.get(securityGroupRule.getSecurityGroupId());
                if (securityGroup == null) {
                    throw new SecurityGroupNotFound();
                }

                //Add security group rule to security group
                securityGroup.getSecurityGroupRules().add(securityGroupRule);
                securityGroupCache.put(securityGroup.getId(), securityGroup);
            }

            //Add security group rules
            Map<String, SecurityGroupRule> securityGroupRuleMap = securityGroupRules
                    .stream()
                    .collect(Collectors.toMap(SecurityGroupRule::getId, Function.identity()));
            securityGroupRuleCache.putAll(securityGroupRuleMap);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deleteSecurityGroupRule(SecurityGroupRule securityGroupRule) throws Exception {
        try (Transaction tx = securityGroupCache.getTransaction().start()) {

            //Delete the security group rule from security group
            SecurityGroup securityGroup = securityGroupCache.get(securityGroupRule.getSecurityGroupId());
            if (securityGroup != null) {
                securityGroup.getSecurityGroupRules().remove(securityGroupRule);
                securityGroupCache.put(securityGroup.getId(), securityGroup);
            }

            //Delete the security group rule
            securityGroupRuleCache.remove(securityGroupRule.getId());

            tx.commit();
        }

    }

    @DurationStatistics
    public SecurityGroupRule getSecurityGroupRule(String id) throws CacheException {
        return securityGroupRuleCache.get(id);
    }

    @DurationStatistics
    public Map<String, SecurityGroupRule> getAllSecurityGroupRules() throws CacheException {
        return securityGroupRuleCache.getAll();
    }

    @DurationStatistics
    public Map<String, SecurityGroupRule> getAllSecurityGroupRules(Map<String, Object[]> queryParams) throws CacheException {
        return securityGroupRuleCache.getAll(queryParams);
    }
}
