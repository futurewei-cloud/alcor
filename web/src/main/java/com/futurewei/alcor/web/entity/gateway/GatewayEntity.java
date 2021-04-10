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

package com.futurewei.alcor.web.entity.gateway;


import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GatewayEntity extends CustomerResource {
    private GatewayType type;
    private String status; // (available)
    private List<GatewayIp> ips;
    private List<String> attachments;
    private List<String> routetables;
    private List<String> tags;
    private String owner;
    private Map<String, String> options;

    public GatewayEntity() {
    }

    public GatewayEntity(String projectId, String id, String name, String description, GatewayType type, String status, List<GatewayIp> ips, List<String> attachments, List<String> routetables, List<String> tags, String owner, Map<String, String> options) {
        super(projectId, id, name, description);
        this.type = type;
        this.status = status;
        this.ips = ips;
        this.attachments = attachments;
        this.routetables = routetables;
        this.tags = tags;
        this.owner = owner;
        this.options = options;
    }
}
