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
package com.futurewei.alcor.common.enumClass;

public enum RouteTableType {

    PUBLIC_SUBNET("public_subnet"),
    PRIVATE_SUBNET("private_subnet"),
    VPC("vpc"),
    NEUTRON_ROUTER("neutron_router"),
    NEUTRON_SUBNET("neutron_subnet");

    private String routeTableType;

    RouteTableType (String env) {
        this.routeTableType = env;
    }

    public String getRouteTableType () {
        return routeTableType;
    }

}
