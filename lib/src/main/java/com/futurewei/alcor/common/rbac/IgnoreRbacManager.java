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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name="token.bind", havingValue="ignore")
public class IgnoreRbacManager implements RbacMangerInterface {
    @Override
    public void checkUpdate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws ResourceNotFoundException {

    }

    @Override
    public void checkGet(String resourceName, TokenEntity tokenEntity, String[] getFields, OwnerChecker ownerChecker) throws ResourceNotFoundException {

    }

    @Override
    public void processGetExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, Object obj) throws ParseObjectException {

    }

    @Override
    public void processListExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, List<Object> objList) throws ParseObjectException {

    }

    @Override
    public boolean isAdmin(String resourceName, TokenEntity tokenEntity) {
        return false;
    }

    @Override
    public void checkDelete(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker) throws ResourceNotFoundException {

    }

    @Override
    public void checkCreate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws ResourceNotFoundException {

    }
}
