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

package com.futurewei.alcor.web.entity.route;

import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
public class RouteEntry extends CustomerResource {

    @NotNull
    private String destination;

    @NotNull
    private String target;

    @NotNull
    private Integer priority;

    @NotNull
    private String routeTableId;

    // gateway_ip_address
    @NotNull
    private String nexthop;

    public RouteEntry() {

    }

    public RouteEntry(String projectId, String Id, String name, String description,
                       String destination, String target, Integer priority, String routeTableId, String nexthop) {
        super(projectId, Id, name, description);
        this.destination = destination;
        this.target = target;
        this.priority = priority;
        this.routeTableId = routeTableId;
        this.nexthop = nexthop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        //if (!super.equals(o)) return false;
        RouteEntry that = (RouteEntry) o;
        return destination.equals(that.destination) && nexthop.equals(that.nexthop);
    }

}
