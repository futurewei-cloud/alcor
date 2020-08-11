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

import java.util.List;
import java.util.Map;

public interface RbacManger {

    void checkUpdate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws ResourceNotFoundException;

    void checkGet(String resourceName, TokenEntity tokenEntity, String[] getFields, OwnerChecker ownerChecker) throws ResourceNotFoundException;

    void processGetExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, Object obj) throws ParseObjectException;

    void processListExcludeFields(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker, List<Object> objList) throws ParseObjectException;

    boolean isAdmin(String resourceName, TokenEntity tokenEntity);

    void checkDelete(String resourceName, TokenEntity tokenEntity, OwnerChecker ownerChecker) throws ResourceNotFoundException;

    void checkCreate(String resourceName, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws ResourceNotFoundException;

}
