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
package com.futurewei.alcor.web.entity.port;

public enum VifType {
    OVS("ovs"),
    MACVTAP("macvtap"),
    HW_VEB("hw_veb"),
    HOSTDEV_PHYSICAL("hostdev_physical"),
    VHOSTUSER("vhostuser"),
    DISTRIBUTED("distributed"),
    OTHER("other");

    private String vifType;

    VifType(String vifType) {
        this.vifType = vifType;
    }

    public String getVifType() {
        return vifType;
    }

    public void setVifType(String vifType) {
        this.vifType = vifType;
    }
}
