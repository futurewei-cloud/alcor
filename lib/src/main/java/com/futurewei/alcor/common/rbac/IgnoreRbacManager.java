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

import com.futurewei.alcor.common.entity.TokenEntity;
import com.futurewei.alcor.common.exception.ParseObjectException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNotValidException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

public class IgnoreRbacManager implements RbacManger {

    private static final String ADMIN_ROLE_NAME = "admin";

    @Override
    public void checkUpdate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws Exception {

    }

    @Override
    public void checkGet(String resourceName, TokenEntity tokenEntity, String[] getFields, OwnerChecker ownerChecker) throws Exception {

    }

    @Override
    public void processGetExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, Object obj) throws Exception {

    }

    @Override
    public void processListExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, List<Object> objList) throws Exception {

    }

    @Override
    public boolean isAdmin(String resourceName, TokenEntity tokenEntity) {
        List<String> roles = tokenEntity.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(ADMIN_ROLE_NAME);
    }

    @Override
    public void checkDelete(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker) throws Exception {

    }

    @Override
    public void checkCreate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws Exception {

    }
}
