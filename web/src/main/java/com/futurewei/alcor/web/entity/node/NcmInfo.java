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
package com.futurewei.alcor.web.entity.node;

import java.util.List;

/**
 * This is the information about an NCM maintained in NMM.
 */
public class NcmInfo {
    public NcmInfo(String ncmId, String ncmUri, int ncmCap) {
        id = ncmId;
        uri = ncmUri;
        cap = ncmCap;
    }

    public NcmInfo(NcmInfo other) {
        id = other.id;
        uri = other.uri;
        cap = other.cap;
    }

    public String getId() { return id; }
    public String getUri() { return uri; }
    public int    getCap() { return cap; }

    public void setId(String ncmId) { id = ncmId; }
    public void setUri(String ncmUri) { uri = ncmUri; }
    public void setCap(int ncmCap) { cap = ncmCap; }

    private String id;
    private String uri;
    private int    cap;
}