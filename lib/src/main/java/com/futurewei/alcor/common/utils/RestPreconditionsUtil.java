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

package com.futurewei.alcor.common.utils;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import org.thymeleaf.util.StringUtils;

public class RestPreconditionsUtil {
    public static void verifyParameterNotNullorEmpty(String resourceId) throws ParameterNullOrEmptyException {
        if (StringUtils.isEmpty(resourceId)) {
            throw new ParameterNullOrEmptyException("Empty parameter");
        }
    }

    public static void verifyParameterNotNullorEmpty(Object resource) throws ParameterNullOrEmptyException {
        if (resource == null) {
            throw new ParameterNullOrEmptyException("null parameter");
        }
    }

    public static void verifyParameterValid(String parameterId, String resourceId) throws ParameterUnexpectedValueException {
        if (parameterId.equals(resourceId) == false) {
            throw new ParameterUnexpectedValueException("parameter values are not valid: parameter id and request body resource id  should be same.");
        }
    }
}