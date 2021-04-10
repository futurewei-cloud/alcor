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


package com.futurewei.alcor.subnet.rbac;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.rbac.OwnerChecker;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.rbac.aspect.OwnerCheckerSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SubnetOwnerCheckerSupplier implements OwnerCheckerSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(SubnetOwnerCheckerSupplier.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SubnetDatabaseService subnetService;

    @Override
    public OwnerChecker getOwnerChecker() {
        return () -> {
            String path = request.getServletPath();
            // only check foreign api
            if (!path.startsWith("/project")) {
                return true;
            }
            String[] pathInfo = path.split("/");
            if (pathInfo.length < 5) {
                return true;
            }
            String projectId = pathInfo[2];
            String subnetId = pathInfo[4];
            try {
                SubnetEntity subnetEntity = subnetService.getBySubnetId(subnetId);
                return projectId.equals(subnetEntity.getProjectId());
            } catch (ResourceNotFoundException | ResourcePersistenceException e) {
                LOG.error("get subnet from db error {}", e.getMessage());
                return false;
            }
        };
    }
}
