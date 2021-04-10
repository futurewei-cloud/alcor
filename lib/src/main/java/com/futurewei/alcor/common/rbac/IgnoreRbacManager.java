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
