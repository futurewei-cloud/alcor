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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.enumClass.OperationType;

public class InternalRouterInfo {
    @JsonProperty("operation_type")
    private OperationType operation_type;

    @JsonProperty("configuration")
    private InternalRouterConfiguration configuration;

    public InternalRouterInfo() {

    }

    public InternalRouterInfo(OperationType operation_type, InternalRouterConfiguration configuration) {
        this.operation_type = operation_type;
        this.configuration = configuration;
    }

    public InternalRouterInfo(InternalRouterInfo routerInfo) {
        this(routerInfo.getOperationType(), routerInfo.getRouterConfiguration());
    }

    public OperationType getOperationType() {
        return this.operation_type;
    }

    public void setOperationType(OperationType operation_type) {
        this.operation_type = operation_type;
    }

    public InternalRouterConfiguration getRouterConfiguration() {
        return this.configuration;
    }

    public void setRouterConfiguration(InternalRouterConfiguration configuration) {
        this.configuration = configuration;
    }
}
