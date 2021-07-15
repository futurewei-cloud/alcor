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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.schema.Common.OperationType;

public class ResourceService {
    protected static final int FORMAT_REVISION_NUMBER = 1;
    protected static final String HOST_DVR_MAC = "fe:16:11:00:00:00";

    protected OperationType getOperationType(com.futurewei.alcor.common.enumClass.OperationType operationType) {
        switch (operationType) {
            case CREATE:
                return OperationType.CREATE;
            case UPDATE:
                return OperationType.UPDATE;
            case INFO:
                return OperationType.INFO;
            case DELETE:
                return OperationType.DELETE;
            default:
                return OperationType.UNRECOGNIZED;
        }
    }
}
