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

package com.futurewei.alcor.dataplane.entity;

/**
 * Subnets Info
 */
public class UTSubnetInfo {

    private String subnetId;

    private String subnetCidr;

    private String subnetGatewayIP;

    private String subnetName;

    private Long tunnelId;

    public UTSubnetInfo() {

    }

    public UTSubnetInfo(String subnetId, String subnetCidr, String subnetGatewayIP, String subnetName, Long tunnelId) {
        this.subnetId = subnetId;
        this.subnetCidr = subnetCidr;
        this.subnetGatewayIP = subnetGatewayIP;
        this.subnetName = subnetName;
        this.tunnelId = tunnelId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getSubnetGatewayIP() {
        return subnetGatewayIP;
    }

    public void setSubnetGatewayIP(String subnetGatewayIP) {
        this.subnetGatewayIP = subnetGatewayIP;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }
}
