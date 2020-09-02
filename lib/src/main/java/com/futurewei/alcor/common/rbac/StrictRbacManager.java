/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.rbac;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.*;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class StrictRbacManager implements RbacManger {
    private static final Logger LOG = LoggerFactory.getLogger(StrictRbacManager.class);

    private static final String DEFAULT_RBAC_FILE_PATH = "config/rbac_policy.json";
    private static final String SERVICE_RBAC_FILE_PATH = "rbac_policy.json";

    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String ADVSVC_ROLE_NAME = "advsvc";

    private ServiceRbacRule serviceRbacRule;

    public StrictRbacManager() throws IOException {
        initServiceRbacRules();
    }

    private void initServiceRbacRules() throws IOException {
        ClassPathResource resource = new ClassPathResource(SERVICE_RBAC_FILE_PATH);
        if (!resource.exists()) {
            resource = new ClassPathResource(DEFAULT_RBAC_FILE_PATH);
            if (!resource.exists()) {
                throw new FileNotFoundException("rbac_policy.json cannot be opened because it does not exist");
            }
        }
        serviceRbacRule = JsonUtil.readValue(resource.getInputStream(), ServiceRbacRule.class);
    }

    @Override
    public void checkUpdate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws Exception {
        String actionName = "update_" + resourceName;
        checkAction(actionName, tokenEntity, bodyFields, ownerChecker);
    }

    @Override
    public void processGetExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker,
                                        Object obj) throws ParseObjectException {
        String actionName = "get_" + resourceName;
        List<String> excludeFields = excludeFields(actionName, tokenEntity, ownerChecker);
        processExcludeFields(excludeFields, obj);
    }

    @Override
    public void processListExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker,
                                         List<Object> objList) throws ParseObjectException {
        String actionName = "get_" + resourceName;
        List<String> excludeFields = excludeFields(actionName, tokenEntity, ownerChecker);
        for (Object obj: objList) {
            processExcludeFields(excludeFields, obj);
        }
    }

    @Override
    public boolean isAdmin(String resourceName, TokenEntity tokenEntity) {
        List<String> tokenRoles = tokenEntity.getRoles();
        if(tokenRoles == null || tokenRoles.isEmpty()) {
            return false;
        }

        return tokenRoles.contains(ADMIN_ROLE_NAME) || tokenRoles.contains(ADVSVC_ROLE_NAME);
    }

    @Override
    public void checkGet(String resourceName, TokenEntity tokenEntity, String[] getFields, OwnerChecker ownerChecker) throws Exception {
        String actionName = "get_" + resourceName;
        List<String> fields = getFields == null ? Collections.emptyList():Arrays.asList(getFields);
        checkAction(actionName, tokenEntity, fields, ownerChecker);
    }

    @Override
    public void checkDelete(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker) throws Exception {
        String actionName = "delete_" + resourceName;
        checkAction(actionName, tokenEntity, null, ownerChecker);
    }

    @Override
    public void checkCreate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields,
                               OwnerChecker ownerChecker) throws Exception {
        String actionName = "create_" + resourceName;
        try {
            checkAction(actionName, tokenEntity, bodyFields, ownerChecker);
        } catch (ResourceNotFoundException e) {
            throw new PolicyNotAuthorizedException(actionName);
        }
    }

    private List<String> excludeFields(String actionName, TokenEntity tokenEntity, OwnerChecker ownerChecker) {
        List<String> excludeFields = new ArrayList<>();
        Optional<ActionRbacRule> rbacRuleOptional = serviceRbacRule.getActionRbacRule(actionName);
        if (rbacRuleOptional.isEmpty()) {
            // if not rbac rule return true
            return excludeFields;
        }

        List<String> tokenRoles = tokenEntity.getRoles();
        List<FieldRbacRule> fieldRbacRules = rbacRuleOptional.get().getFieldRbacRules();
        for (FieldRbacRule fieldRbacRule: fieldRbacRules) {
            boolean valid = checkRule(fieldRbacRule.getRuleType(), tokenRoles,
                    fieldRbacRule.getRoles(), ownerChecker);
            if(!valid) {
                // LOG.info("{}:{} check policy rule failed", actionName, fieldRbacRule.getName());
                excludeFields.add(fieldRbacRule.getName());
            }
        }
        return excludeFields;
    }

    private void processExcludeFields(List<String> excludeFields, Object obj) throws ParseObjectException {
        if (excludeFields == null || excludeFields.isEmpty()) {
            return;
        }

        Field[] fields = ControllerUtil.getAllDeclaredFields(obj.getClass());
        for (Field field: fields) {
            JsonProperty jsonAnnotate = field.getAnnotation(JsonProperty.class);
            String fieldName = jsonAnnotate == null ? field.getName(): jsonAnnotate.value();
            if (excludeFields.contains(fieldName)) {
                // this field should not visible for this user
                field.setAccessible(true);
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e) {
                    LOG.error("parse data error {}", e.getMessage());
                    throw new ParseObjectException();
                }
            }
        }
    }

    private void checkAction(String actionName, TokenEntity tokenEntity, List<String> bodyFields,
                             OwnerChecker ownerChecker) throws Exception {
        Optional<ActionRbacRule> rbacRuleOptional = serviceRbacRule.getActionRbacRule(actionName);
        if (rbacRuleOptional.isEmpty()) {
            // if not rbac rule return true
            return;
        }

        String ruleType = rbacRuleOptional.get().getRuleType();
        List<String> tokenRoles = tokenEntity.getRoles();

        boolean actionValid = checkRule(ruleType, tokenRoles, rbacRuleOptional.get().getRoles(), ownerChecker);
        if (!actionValid) {
            LOG.warn("{} check policy failed", actionName);
            throw new PolicyResourceNotFoundException();
        }

        checkFields(rbacRuleOptional.get(), tokenEntity, bodyFields, ownerChecker);
    }

    private void checkFields(ActionRbacRule actionRbacRule, TokenEntity tokenEntity,
                                List<String> bodyFields, OwnerChecker ownerChecker) throws FieldPolicyNotAuthorizedException {
        List<FieldRbacRule> fieldRbacRules = actionRbacRule.getFieldRbacRules();
        if (fieldRbacRules == null || fieldRbacRules.isEmpty() || bodyFields == null) {
            return;
        }

        for (FieldRbacRule fieldRbacRule: fieldRbacRules) {
            // only need check bodyFields fields
            if (!bodyFields.contains(fieldRbacRule.getName())) {
                continue;
            }

            boolean valid = checkRule(fieldRbacRule.getRuleType(), tokenEntity.getRoles(),
                    fieldRbacRule.getRoles(), ownerChecker);
            if (!valid) {
                LOG.warn("{}:{} check policy rule failed", actionRbacRule.getName(), fieldRbacRule.getName());
                throw new FieldPolicyNotAuthorizedException(actionRbacRule.getName(), fieldRbacRule.getName());
            }
        }
    }

    private boolean checkRule(String ruleType, List<String> tokenRoles, List<String> ruleRoles,
                              OwnerChecker ownerChecker) {
        // rule roles is empty, so every role can be ok
        if (ruleRoles == null) {
            ruleRoles = Collections.emptyList();
        }

        // token roles is empty, but rule need some role, return false directly
        if (tokenRoles == null || tokenRoles.isEmpty()) {
            return false;
        }

        if (RuleType.RULE_ANY.equals(ruleType)) {
            return true;
        } else if (RuleType.ADMIN_ONLY.equals(ruleType)){
            return tokenRoles.contains(ADMIN_ROLE_NAME);
        } else if (RuleType.ADMIN_OR_OWNER.equals(ruleType)) {
            return tokenRoles.contains(ADMIN_ROLE_NAME) || ownerChecker.apply();
        } else if (RuleType.MUlTI_ROLES.equals(ruleType)) {
            return ruleRoles.isEmpty() || ruleRoles.stream().anyMatch(tokenRoles::contains);
        } else if (RuleType.MUlTI_ROLES_OR_OWNER.equals(ruleType)) {
            return ruleRoles.isEmpty() || ruleRoles.stream().anyMatch(tokenRoles::contains) || ownerChecker.apply();
        } else {
            // maybe rule defined error, this can't block, return true
            LOG.warn("unknown defined rbac rule type {}", ruleType);
            return true;
        }
    }
}
