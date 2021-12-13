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

package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.web.entity.route.RouteEntry;

public class InternalSubnetRouterMap {
    private String routerId;
    private String subnetId;

    public InternalSubnetRouterMap() { };

    public InternalSubnetRouterMap(String routerId, String subnetId) {
        this.routerId = routerId;
        this.subnetId = subnetId;
    }

    public void setRouterId(String routerId) { this.routerId = routerId; }

    public void setSubnetIds(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getRouterId() { return this.routerId; }

    public String getSubnetId() {
        return this.subnetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        //if (!super.equals(o)) return false;
        InternalSubnetRouterMap internalSubnetRouterMap = (InternalSubnetRouterMap) o;
        return routerId.equals(internalSubnetRouterMap.routerId) && subnetId.equals(internalSubnetRouterMap.subnetId);
    }

    @Override
    public int hashCode()
    {
        return 31 * subnetId.hashCode() + routerId.hashCode();
    }
}