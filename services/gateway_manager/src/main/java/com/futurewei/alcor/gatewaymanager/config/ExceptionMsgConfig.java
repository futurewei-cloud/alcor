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
package com.futurewei.alcor.gatewaymanager.config;

public enum ExceptionMsgConfig {

    GATEWAY_ENTITY_NOT_FOUND("GatewayEntity could not be found"),
    ATTACHMENT_NOT_FOUND("Attachment could not be found"),
    GATEWAY_NOT_ASSOCIATED_ATTACHMENT("GatewayEntity not be associated the attachment"),
    VPCINFO_PARAMETER_ILLEGAL("VpcInfo parameters illegal,may be have null parameter or other situation"),
    RESOURCE_ID_IS_NULL("GatewayInfo's resource_id is null"),
    GATEWAYS_IS_NULL("GatewayInfo's gateways is null or empty"),
    GATEWAY_TYPE_OR_STATUS_IS_NULL("GatewayEntity's type or status is null"),
    GATEWAYINFO_NOT_FOUND("GatewayInfo could not be found"),
    ROLLBACK_FAILED("rollback failed");

    private String msg;

    ExceptionMsgConfig(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
