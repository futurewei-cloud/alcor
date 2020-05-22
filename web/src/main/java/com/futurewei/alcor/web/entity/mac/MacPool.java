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
package com.futurewei.alcor.macmanager.entity;

import java.io.Serializable;
import java.util.HashSet;

public class MacPool implements Serializable {
    private String rangeId;
    private HashSet<String> setMac;

    public MacPool() {
        rangeId = "";
        setMac = new HashSet<String>();
    }

    public MacPool(MacPool pool) {
        this(pool.rangeId, pool.setMac);
    }

    public MacPool(String rangeId, HashSet<String> macAddressSet) {
        this.rangeId = rangeId;
        this.setMac = macAddressSet;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public HashSet<String> getSetMac() {
        return setMac;
    }

    public void setSetMac(HashSet<String> setMac) {
        this.setMac = setMac;
    }

    public void setMacAddresses(HashSet<String> setMac) {
        this.setMac.addAll(setMac);
    }
}
