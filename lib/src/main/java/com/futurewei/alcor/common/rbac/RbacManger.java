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

import java.util.List;
import java.util.Map;

/**
 *  A Rbac decide whether user token can operate the resource
 *  if check failed ,throw a exception
 */
public interface RbacManger {

    /**
     * check user whether have the permission to update the resource
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param bodyFields http put request body fields
     * @param ownerChecker a checker which check whether user is owner of the resource
     * @throws Exception if check failed, throw a exception
     */
    void checkUpdate(String resource, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws Exception;

    /**
     * check user whether have the permission to get the resource and resource fields.
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param getFields http get request url  fields
     * @param ownerChecker a checker which check whether user is owner of the resouce
     * @throws Exception if check failed, throw a exception
     */
    void checkGet(String resource, TokenEntity tokenEntity, String[] getFields, OwnerChecker ownerChecker) throws Exception;

    /**
     * remove resource fields that user have no permission to see.
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param ownerChecker a checker which check whether user is owner of the resource
     * @throws Exception if check failed, throw a exception
     */
    void processGetExcludeFields(String resource, TokenEntity tokenEntity, OwnerChecker ownerChecker, Object obj) throws Exception;

    /**
     * remove resource fields that user have no permission to see.
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param ownerChecker a checker which check whether user is owner of the resource
     * @param objList http response objects
     * @throws Exception if check failed, throw a exception
     */
    void processListExcludeFields(String resource, TokenEntity tokenEntity, OwnerChecker ownerChecker, List<Object> objList) throws Exception;

    /**
     * check user whether is admin user.
     * @param resource the resource name
     * @param tokenEntity the user token info
     */
    boolean isAdmin(String resource, TokenEntity tokenEntity);

    /**
     * check user whether have the permission to delete the resource
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param ownerChecker a checker which check whether user is owner of the resouce
     * @throws Exception if check failed, throw a exception
     */
    void checkDelete(String resource, TokenEntity tokenEntity, OwnerChecker ownerChecker) throws Exception;

    /**
     * check user whether have the permission to create the resource
     * @param resource the resource name
     * @param tokenEntity the user token info
     * @param bodyFields http put request body fields
     * @param ownerChecker a checker which check whether user is owner of the resouce
     * @throws Exception if check failed, throw a exception
     */
    void checkCreate(String resource, TokenEntity tokenEntity, List<String> bodyFields, OwnerChecker ownerChecker) throws Exception;

}
