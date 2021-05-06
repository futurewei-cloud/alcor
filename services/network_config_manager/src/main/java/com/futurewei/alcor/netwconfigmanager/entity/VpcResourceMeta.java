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
package com.futurewei.alcor.netwconfigmanager.entity;

import java.util.HashMap;
import java.util.List;

public class VpcResourceMeta {

    private String vni;

    // Private IP => ResourceMetadata
    private HashMap<String, ResourceMeta> resourceMetaMap;

    public VpcResourceMeta(String vni, HashMap<String, ResourceMeta> resourceMetaMap) {
        this.vni = vni;
        this.resourceMetaMap = new HashMap<>(resourceMetaMap);
    }

    public String getVni() {
        return this.vni;
    }

    public HashMap<String, ResourceMeta> getResourceMetaMap() {
        return this.resourceMetaMap;
    }

    public ResourceMeta getResourceMetas(String privateIP) {
        if (this.resourceMetaMap == null || !this.resourceMetaMap.containsKey(privateIP)) {
            return null;
        }

        return this.resourceMetaMap.get(privateIP);
    }

    public void setResourceMetas(String privateIP, ResourceMeta portAssociatedResourceMeta) {
        this.resourceMetaMap.put(privateIP, portAssociatedResourceMeta);
    }
}
