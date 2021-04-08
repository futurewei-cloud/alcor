package com.futurewei.alcor.web.entity.node;

/**
 * This is the information about an NCM maintained in NMM.
 * So, the question is why does NMM know about uri and capacity of NCM?
 * NMM can manage just by keeping NCM Id, Node Id. If NCM uri is needed,
 * NMM can query the NCM, same thing with capacity, including updates to these
 * values.
 */
public class NcmInfo {
    public NcmInfo(String ncmId, String ncmUri, int ncmCap) {
        id = ncmId;
        uri = ncmUri;
        cap = ncmCap;
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
