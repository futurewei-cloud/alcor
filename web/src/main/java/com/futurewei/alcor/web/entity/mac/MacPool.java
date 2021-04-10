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
