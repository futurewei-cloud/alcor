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
 * So, the question is why does NMM know about uri and capacity of NCM?
 * NMM can manage just by keeping NCM Id, Node Id. If NCM uri is needed,
 * NMM can query the NCM, same thing with capacity, including updates to these
 * values.
 */
public class NcmInfo {
    public NcmInfo(String ncmId, String ncmUri, int ncmCap, List<String> nodeIds) {
        id = ncmId;
        uri = ncmUri;
        cap = ncmCap;
        nodes = nodeIds;
    }

    public String getId() { return id; }
    public String getUri() { return uri; }
    public int    getCap() { return cap; }
    public List<String> getNodes(String ncmId) { return nodes; }

    public void setId(String ncmId) { id = ncmId; }
    public void setUri(String ncmUri) { uri = ncmUri; }
    public void setCap(int ncmCap) { cap = ncmCap; }
    public void setNodes(List<String> nodeIds) {nodes = nodeIds;}
    public void appendNodes(List<String> newNodes) { nodes.addAll(newNodes); }
    public void removeNodes(List<String> newNodes) { nodes.removeAll(newNodes); }

    private String id;
    private String uri;
    private int    cap;
    private List<String> nodes;
}
