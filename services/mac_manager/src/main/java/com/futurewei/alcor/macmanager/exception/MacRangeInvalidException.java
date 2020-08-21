/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.macmanager.exception;

import com.futurewei.alcor.macmanager.utils.MacManagerConstant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_INVALID)
public class MacRangeInvalidException extends Exception {
    public MacRangeInvalidException() {
        super(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_INVALID);
    }

    public MacRangeInvalidException(String message) {
        super(message);
    }

    public MacRangeInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public MacRangeInvalidException(Throwable cause) {
        super(cause);
    }
}
