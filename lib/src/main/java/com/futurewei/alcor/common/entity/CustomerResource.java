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

package com.futurewei.alcor.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.Objects;

public class CustomerResource extends Resource {

    @JsonProperty("project_id")
    @QuerySqlField(index = true)
    private String projectId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    public CustomerResource() {

    }

    public CustomerResource(CustomerResource state) {
        this(state.projectId, state.getId(), state.name, state.description);
    }

    public CustomerResource(String projectId, String id, String name, String description) {
        super(id);
        this.projectId = projectId;
        this.tenantId = projectId;
        this.name = name;
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
        if (this.tenantId == null) {
            this.tenantId = projectId;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerResource that = (CustomerResource) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(this.getId(), that.getId()) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, this.getId(), name, description);
    }

    @Override
    public String toString() {
        return "CustomerResource{" +
                "projectId='" + projectId + '\'' +
                ", id='" + this.getId() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}