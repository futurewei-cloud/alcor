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