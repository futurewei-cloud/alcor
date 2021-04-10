/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
